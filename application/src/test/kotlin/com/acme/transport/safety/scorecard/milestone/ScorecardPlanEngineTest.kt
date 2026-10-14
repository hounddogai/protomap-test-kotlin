package com.acme.transport.safety.scorecard.milestone

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class ScorecardPlanEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultScorecardPlanEngine(newRegistry())
        val item = newScorecardPlan("code-892", "label-342")
        val result = engine.evaluate(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
