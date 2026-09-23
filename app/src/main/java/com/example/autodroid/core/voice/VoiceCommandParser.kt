package com.example.autodroid.core.voice

enum class VoiceAction {
    FUEL,
    NAVIGATE_HOME,
    UNKNOWN
}

object VoiceCommandParser {
    fun parse(command: String): VoiceAction {
        val lower = command.lowercase()
        return when {
            lower.contains("fuel") -> VoiceAction.FUEL
            lower.contains("navigate") && lower.contains("home") -> VoiceAction.NAVIGATE_HOME
            lower.contains("navigate") -> VoiceAction.NAVIGATE_HOME // Broad match for demo
            else -> VoiceAction.UNKNOWN
        }
    }
}
