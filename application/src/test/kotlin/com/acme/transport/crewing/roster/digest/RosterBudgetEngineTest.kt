package com.acme.transport.crewing.roster.digest

import kotlin.test.Test
import kotlin.test.assertTrue

class RosterBudgetEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultRosterBudgetEngine()
        val item = newRosterBudget("code-264", "title-508")
        val result = engine.run(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
