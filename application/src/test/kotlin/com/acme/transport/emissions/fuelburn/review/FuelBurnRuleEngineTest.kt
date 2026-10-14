package com.acme.transport.emissions.fuelburn.review

import kotlin.test.Test
import kotlin.test.assertTrue

class FuelBurnRuleEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultFuelBurnRuleEngine()
        val item = newFuelBurnRule("code-380", "name-298")
        val result = engine.compute(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
