package com.acme.transport.terminal.dwell.exception

import kotlin.test.Test
import kotlin.test.assertTrue

class LegacyDwellPreferenceEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultLegacyDwellPreferenceEngine()
        val item = newLegacyDwellPreference("code-579", "name-199")
        val result = engine.compute(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
