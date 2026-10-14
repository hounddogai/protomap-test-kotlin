package com.acme.transport.crewing.restbreak.policy

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class GlobalRestBreakBudgetEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultGlobalRestBreakBudgetEngine(newRegistry())
        val item = newGlobalRestBreakBudget("code-164", "label-221")
        val result = engine.price(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
