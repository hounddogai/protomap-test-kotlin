package com.acme.transport.emissions.evcharge.chargepreference

import kotlin.test.Test
import kotlin.test.assertTrue

class CappedEvChargePreferenceEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultCappedEvChargePreferenceEngine()
        val item = newCappedEvChargePreference("code-401", "label-450")
        val result = engine.price(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
