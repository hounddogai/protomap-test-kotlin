package com.acme.transport.emissions.fuelburn.claim

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class WeightedFuelBurnBudgetEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultWeightedFuelBurnBudgetEngine(newRegistry())
        val item = newWeightedFuelBurnBudget("code-888", "name-952")
        val result = engine.run(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
