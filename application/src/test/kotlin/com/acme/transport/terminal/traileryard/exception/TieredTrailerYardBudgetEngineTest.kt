package com.acme.transport.terminal.traileryard.exception

import kotlin.test.Test
import kotlin.test.assertTrue

class TieredTrailerYardBudgetEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultTieredTrailerYardBudgetEngine()
        val item = newTieredTrailerYardBudget("code-599", "label-784")
        val result = engine.price(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
