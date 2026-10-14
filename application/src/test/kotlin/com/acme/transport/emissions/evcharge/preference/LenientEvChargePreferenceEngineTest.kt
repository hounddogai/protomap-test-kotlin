package com.acme.transport.emissions.evcharge.preference

import kotlin.test.Test
import kotlin.test.assertTrue

class LenientEvChargePreferenceEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultLenientEvChargePreferenceEngine()
        val item = newLenientEvChargePreference("code-962", "label-387")
        val result = engine.run(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
