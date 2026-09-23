package com.example.autodroid.core.voice

import android.content.Context
import android.speech.tts.TextToSpeech
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton

enum class VoiceState {
    IDLE, LISTENING, PROCESSING, SPEAKING
}

@Singleton
class VoiceAssistantManager @Inject constructor(
    @ApplicationContext private val context: Context
) : TextToSpeech.OnInitListener {

    private var tts: TextToSpeech? = null

    private val _voiceState = MutableStateFlow(VoiceState.IDLE)
    val voiceState: StateFlow<VoiceState> = _voiceState.asStateFlow()

    private val _transcript = MutableStateFlow("")
    val transcript: StateFlow<String> = _transcript.asStateFlow()

    private var speechRecognizer: android.speech.SpeechRecognizer? = null

    init {
        tts = TextToSpeech(context, this)
        if (android.speech.SpeechRecognizer.isRecognitionAvailable(context)) {
            speechRecognizer = android.speech.SpeechRecognizer.createSpeechRecognizer(context)
        }
    }

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            tts?.language = Locale.getDefault()
        }
    }

    fun startListening(onCommandRecognized: (String) -> Unit) {
        if (speechRecognizer != null && androidx.core.content.ContextCompat.checkSelfPermission(context, android.Manifest.permission.RECORD_AUDIO) == android.content.pm.PackageManager.PERMISSION_GRANTED) {
            val intent = android.content.Intent(android.speech.RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                putExtra(android.speech.RecognizerIntent.EXTRA_LANGUAGE_MODEL, android.speech.RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            }
            
            speechRecognizer?.setRecognitionListener(object : android.speech.RecognitionListener {
                override fun onReadyForSpeech(params: android.os.Bundle?) {
                    _voiceState.value = VoiceState.LISTENING
                    _transcript.value = "Listening..."
                }
                override fun onBeginningOfSpeech() {}
                override fun onRmsChanged(rmsdB: Float) {}
                override fun onBufferReceived(buffer: ByteArray?) {}
                override fun onEndOfSpeech() {
                    _voiceState.value = VoiceState.PROCESSING
                }
                override fun onError(error: Int) {
                    _voiceState.value = VoiceState.IDLE
                    _transcript.value = "Error $error"
                }
                override fun onResults(results: android.os.Bundle?) {
                    val matches = results?.getStringArrayList(android.speech.SpeechRecognizer.RESULTS_RECOGNITION)
                    val command = matches?.firstOrNull() ?: ""
                    _transcript.value = command
                    onCommandRecognized(command)
                }
                override fun onPartialResults(partialResults: android.os.Bundle?) {}
                override fun onEvent(eventType: Int, params: android.os.Bundle?) {}
            })
            
            android.os.Handler(android.os.Looper.getMainLooper()).post {
                speechRecognizer?.startListening(intent)
            }
        } else {
            // Fallback to simulate for emulator demo
            simulateListen("How is the fuel?", onCommandRecognized)
        }
    }

    // Simulate speech recognition for the POC
    fun simulateListen(command: String, onCommandRecognized: (String) -> Unit) {
        _voiceState.value = VoiceState.LISTENING
        _transcript.value = "Listening..."
        
        android.os.Handler(android.os.Looper.getMainLooper()).postDelayed({
            _voiceState.value = VoiceState.PROCESSING
            _transcript.value = command
            
            android.os.Handler(android.os.Looper.getMainLooper()).postDelayed({
                onCommandRecognized(command)
            }, 1000)
            
        }, 1500)
    }

    fun speak(text: String) {
        _voiceState.value = VoiceState.SPEAKING
        _transcript.value = text
        tts?.speak(text, TextToSpeech.QUEUE_FLUSH, null, "TTS_ID")
        
        android.os.Handler(android.os.Looper.getMainLooper()).postDelayed({
            _voiceState.value = VoiceState.IDLE
            _transcript.value = ""
        }, 3000)
    }
}
