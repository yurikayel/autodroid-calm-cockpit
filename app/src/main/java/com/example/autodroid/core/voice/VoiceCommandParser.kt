package com.example.autodroid.core.voice

sealed class VoiceAction {
    data object FUEL : VoiceAction()
    data object NAVIGATE_HOME : VoiceAction()
    data class WEATHER(val city: String) : VoiceAction()
    data object WEATHER_ALONG_ROUTE : VoiceAction()
    data object UNKNOWN : VoiceAction()
}

/**
 * Parses raw voice transcript text into a typed [VoiceAction].
 * Uses word-boundary-aware matching to avoid false positives
 * (e.g. "Campinas" no longer matches "in").
 */
object VoiceCommandParser {

    private val WEATHER_IN_EN = Regex("""\bweather\s+in\s+(.+)""", RegexOption.IGNORE_CASE)
    private val WEATHER_EM_PT = Regex("""\b(?:previsão|tempo)\s+em\s+(.+)""", RegexOption.IGNORE_CASE)

    fun parse(command: String): VoiceAction {
        val lower = command.lowercase()
        return when {
            lower.contains("weather on the way") || lower.contains("clima no caminho") -> VoiceAction.WEATHER_ALONG_ROUTE
            else -> parseWeatherCity(command)
                ?: parseSimpleKeywords(lower)
        }
    }

    private fun parseWeatherCity(command: String): VoiceAction? {
        WEATHER_IN_EN.find(command)?.let { return VoiceAction.WEATHER(it.groupValues[1].trim()) }
        WEATHER_EM_PT.find(command)?.let { return VoiceAction.WEATHER(it.groupValues[1].trim()) }
        return null
    }

    private fun parseSimpleKeywords(lower: String): VoiceAction = when {
        lower.contains("weather") || lower.contains("previsão") -> VoiceAction.WEATHER("")
        lower.contains("fuel") -> VoiceAction.FUEL
        lower.contains("navigate") -> VoiceAction.NAVIGATE_HOME
        else -> VoiceAction.UNKNOWN
    }
}
