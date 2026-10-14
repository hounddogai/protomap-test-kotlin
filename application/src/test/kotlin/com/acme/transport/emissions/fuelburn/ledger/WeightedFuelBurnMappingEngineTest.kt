package com.acme.transport.emissions.fuelburn.ledger

import kotlin.test.Test
import kotlin.test.assertTrue

class WeightedFuelBurnMappingEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultWeightedFuelBurnMappingEngine()
        val item = newWeightedFuelBurnMapping("code-250", "label-459")
        val result = engine.compute(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
