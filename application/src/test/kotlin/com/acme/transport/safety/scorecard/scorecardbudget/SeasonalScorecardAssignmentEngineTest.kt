package com.acme.transport.safety.scorecard.scorecardbudget

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class SeasonalScorecardAssignmentEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultSeasonalScorecardAssignmentEngine(newRegistry())
        val item = newSeasonalScorecardAssignment("code-468", "label-6")
        val result = engine.compute(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
