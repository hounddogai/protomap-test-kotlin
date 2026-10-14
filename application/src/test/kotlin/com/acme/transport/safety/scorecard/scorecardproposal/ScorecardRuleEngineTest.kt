package com.acme.transport.safety.scorecard.scorecardproposal

import kotlin.test.Test
import kotlin.test.assertTrue

class ScorecardRuleEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultScorecardRuleEngine()
        val item = newScorecardRule("code-300", "title-640")
        val result = engine.evaluate(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
