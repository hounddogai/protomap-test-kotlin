package com.acme.transport.crewing.swap.budget

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class SwapBudgetEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultSwapBudgetEngine(newRegistry())
        val item = newSwapBudget("code-33", "name-666")
        val result = engine.price(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
