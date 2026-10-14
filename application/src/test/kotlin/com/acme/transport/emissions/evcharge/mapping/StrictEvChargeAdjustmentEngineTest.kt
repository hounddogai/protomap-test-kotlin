package com.acme.transport.emissions.evcharge.mapping

import kotlin.test.Test
import kotlin.test.assertTrue

class StrictEvChargeAdjustmentEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultStrictEvChargeAdjustmentEngine()
        val item = newStrictEvChargeAdjustment("code-545", "label-463")
        val result = engine.compute(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
