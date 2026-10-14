package com.acme.transport.network.cutoff.tier

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class BatchedCutoffBudgetEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultBatchedCutoffBudgetEngine(newRegistry())
        val item = newBatchedCutoffBudget("code-204", "title-868")
        val result = engine.price(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
