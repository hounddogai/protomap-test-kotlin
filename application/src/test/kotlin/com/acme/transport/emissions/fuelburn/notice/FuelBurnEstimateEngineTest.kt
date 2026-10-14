package com.acme.transport.emissions.fuelburn.notice

import kotlin.test.Test
import kotlin.test.assertTrue

class FuelBurnEstimateEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultFuelBurnEstimateEngine()
        val item = newFuelBurnEstimate("code-866", "title-228")
        val result = engine.compute(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
