package com.acme.transport.safety.coaching.rollup

import kotlin.test.Test
import kotlin.test.assertTrue

class DefaultCoachingBudgetEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultDefaultCoachingBudgetEngine()
        val item = newDefaultCoachingBudget("code-833", "label-173")
        val result = engine.evaluate(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
