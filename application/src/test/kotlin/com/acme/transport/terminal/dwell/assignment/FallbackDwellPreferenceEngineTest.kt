package com.acme.transport.terminal.dwell.assignment

import kotlin.test.Test
import kotlin.test.assertTrue

class FallbackDwellPreferenceEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultFallbackDwellPreferenceEngine()
        val item = newFallbackDwellPreference("code-492", "name-171")
        val result = engine.run(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
