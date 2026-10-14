package com.acme.transport.emissions.fuelburn.audit

import kotlin.test.Test
import kotlin.test.assertTrue

class WeightedFuelBurnTargetEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultWeightedFuelBurnTargetEngine()
        val item = newWeightedFuelBurnTarget("code-217", "name-553")
        val result = engine.evaluate(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
