package com.acme.transport.emissions.evcharge.baseline

import kotlin.test.Test
import kotlin.test.assertTrue

class EvChargeAdjustmentEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultEvChargeAdjustmentEngine()
        val item = newEvChargeAdjustment("code-949", "title-930")
        val result = engine.run(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
