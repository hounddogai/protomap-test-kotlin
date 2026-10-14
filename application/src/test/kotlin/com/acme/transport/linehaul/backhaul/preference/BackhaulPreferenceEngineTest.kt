package com.acme.transport.linehaul.backhaul.preference

import kotlin.test.Test
import kotlin.test.assertTrue

class BackhaulPreferenceEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultBackhaulPreferenceEngine()
        val item = newBackhaulPreference("code-393", "name-147")
        val result = engine.compute(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
