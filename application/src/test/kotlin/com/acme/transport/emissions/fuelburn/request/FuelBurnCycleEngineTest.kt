package com.acme.transport.emissions.fuelburn.request

import kotlin.test.Test
import kotlin.test.assertTrue

class FuelBurnCycleEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultFuelBurnCycleEngine()
        val item = newFuelBurnCycle("code-535", "name-510")
        val result = engine.compute(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
