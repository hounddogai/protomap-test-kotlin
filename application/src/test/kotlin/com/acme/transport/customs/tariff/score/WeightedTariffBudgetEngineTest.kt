package com.acme.transport.customs.tariff.score

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class WeightedTariffBudgetEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultWeightedTariffBudgetEngine(newRegistry())
        val item = newWeightedTariffBudget("code-282", "title-449")
        val result = engine.compute(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
