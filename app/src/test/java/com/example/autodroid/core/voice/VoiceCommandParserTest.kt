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
    fun `parse unknown command returns UNKNOWN action`() {
        val result = VoiceCommandParser.parse("what is the weather")
        assertEquals(VoiceAction.UNKNOWN, result)
    }
}
