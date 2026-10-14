package com.acme.transport.emissions.fuelburn.baseline

import kotlin.test.Test
import kotlin.test.assertTrue

class FuelBurnBaselineEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultFuelBurnBaselineEngine()
        val item = newFuelBurnBaseline("code-994", "name-228")
        val result = engine.run(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
