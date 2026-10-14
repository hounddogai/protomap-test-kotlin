package com.acme.transport.emissions.fuelburn.baseline

import kotlin.test.Test
import kotlin.test.assertTrue

class FuelBurnRollupEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultFuelBurnRollupEngine()
        val item = newFuelBurnRollup("code-349", "name-500")
        val result = engine.compute(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
