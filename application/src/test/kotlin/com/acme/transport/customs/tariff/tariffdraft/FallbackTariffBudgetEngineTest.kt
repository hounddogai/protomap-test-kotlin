package com.acme.transport.customs.tariff.tariffdraft

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class FallbackTariffBudgetEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultFallbackTariffBudgetEngine(newRegistry())
        val item = newFallbackTariffBudget("code-299", "label-783")
        val result = engine.compute(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
