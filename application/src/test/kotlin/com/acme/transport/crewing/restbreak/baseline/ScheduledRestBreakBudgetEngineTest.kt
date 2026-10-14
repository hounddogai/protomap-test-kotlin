package com.acme.transport.crewing.restbreak.baseline

import kotlin.test.Test
import kotlin.test.assertTrue

class ScheduledRestBreakBudgetEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultScheduledRestBreakBudgetEngine()
        val item = newScheduledRestBreakBudget("code-897", "title-230")
        val result = engine.run(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
