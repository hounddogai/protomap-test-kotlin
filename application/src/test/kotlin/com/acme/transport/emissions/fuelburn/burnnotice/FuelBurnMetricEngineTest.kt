package com.acme.transport.emissions.fuelburn.burnnotice

import kotlin.test.Test
import kotlin.test.assertTrue

class FuelBurnMetricEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultFuelBurnMetricEngine()
        val item = newFuelBurnMetric("code-716", "name-126")
        val result = engine.compute(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
