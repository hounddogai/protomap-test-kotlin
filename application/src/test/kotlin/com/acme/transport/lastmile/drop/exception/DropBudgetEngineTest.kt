package com.acme.transport.lastmile.drop.exception

import kotlin.test.Test
import kotlin.test.assertTrue

class DropBudgetEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultDropBudgetEngine()
        val item = newDropBudget("code-177", "name-412")
        val result = engine.evaluate(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
