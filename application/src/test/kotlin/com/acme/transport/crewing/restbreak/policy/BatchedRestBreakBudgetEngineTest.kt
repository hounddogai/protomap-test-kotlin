package com.acme.transport.crewing.restbreak.policy

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class BatchedRestBreakBudgetEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultBatchedRestBreakBudgetEngine(newRegistry())
        val item = newBatchedRestBreakBudget("code-555", "name-467")
        val result = engine.price(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
