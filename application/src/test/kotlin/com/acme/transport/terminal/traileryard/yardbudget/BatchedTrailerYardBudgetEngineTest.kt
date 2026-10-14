package com.acme.transport.terminal.traileryard.yardbudget

import kotlin.test.Test
import kotlin.test.assertTrue

class BatchedTrailerYardBudgetEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultBatchedTrailerYardBudgetEngine()
        val item = newBatchedTrailerYardBudget("code-198", "name-997")
        val result = engine.price(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
