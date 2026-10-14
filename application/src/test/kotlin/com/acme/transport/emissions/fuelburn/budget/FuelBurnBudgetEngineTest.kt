package com.acme.transport.emissions.fuelburn.budget

import kotlin.test.Test
import kotlin.test.assertTrue

class FuelBurnBudgetEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultFuelBurnBudgetEngine()
        val item = newFuelBurnBudget("code-452", "title-507")
        val result = engine.run(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
