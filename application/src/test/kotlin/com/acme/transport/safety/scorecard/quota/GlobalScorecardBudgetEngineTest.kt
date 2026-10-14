package com.acme.transport.safety.scorecard.quota

import kotlin.test.Test
import kotlin.test.assertTrue

class GlobalScorecardBudgetEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultGlobalScorecardBudgetEngine()
        val item = newGlobalScorecardBudget("code-112", "name-281")
        val result = engine.price(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
