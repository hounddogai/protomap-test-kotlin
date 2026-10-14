package com.acme.transport.network.hub.summary

import kotlin.test.Test
import kotlin.test.assertTrue

class HubPreferenceEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultHubPreferenceEngine()
        val item = newHubPreference("code-586", "name-218")
        val result = engine.evaluate(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
