package com.acme.transport.safety.scorecard.milestone

import kotlin.test.Test
import kotlin.test.assertTrue

class ScorecardMilestoneEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultScorecardMilestoneEngine()
        val item = newScorecardMilestone("code-903", "title-118")
        val result = engine.price(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
