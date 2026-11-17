package com.example.autodroid.core.voice

import org.junit.Assert.assertEquals
import org.junit.Test

class VoiceCommandParserTest {

    @Test
    fun `parse fuel command returns FUEL action`() {
        val result = VoiceCommandParser.parse("how is the fuel level")
        assertEquals(VoiceAction.FUEL, result)
    }

    @Test
    fun `parse navigate command returns NAVIGATE_HOME action`() {
        val result = VoiceCommandParser.parse("navigate home please")
        assertEquals(VoiceAction.NAVIGATE_HOME, result)
    }

    @Test
    fun `parse generic weather command returns WEATHER with empty city`() {
        val result = VoiceCommandParser.parse("what is the weather")
        assertEquals(VoiceAction.WEATHER(""), result)
    }

    @Test
    fun `parse weather in city returns WEATHER with city`() {
        val result = VoiceCommandParser.parse("weather in Campinas")
        assertEquals(VoiceAction.WEATHER("Campinas"), result)
    }

    @Test
    fun `parse weather along route returns WEATHER_ALONG_ROUTE`() {
        val result = VoiceCommandParser.parse("show me weather on the way")
        assertEquals(VoiceAction.WEATHER_ALONG_ROUTE, result)
    }

    @Test
    fun `parse clima no caminho returns WEATHER_ALONG_ROUTE`() {
        val result = VoiceCommandParser.parse("clima no caminho")
        assertEquals(VoiceAction.WEATHER_ALONG_ROUTE, result)
    }

    @Test
    fun `parse unknown command returns UNKNOWN action`() {
        val result = VoiceCommandParser.parse("sing a song")
        assertEquals(VoiceAction.UNKNOWN, result)
    }
}
