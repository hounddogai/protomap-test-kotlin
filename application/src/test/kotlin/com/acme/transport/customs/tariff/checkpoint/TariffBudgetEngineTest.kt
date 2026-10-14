package com.acme.transport.customs.tariff.checkpoint

import kotlin.test.Test
import kotlin.test.assertTrue

class TariffBudgetEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultTariffBudgetEngine()
        val item = newTariffBudget("code-499", "name-705")
        val result = engine.price(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
