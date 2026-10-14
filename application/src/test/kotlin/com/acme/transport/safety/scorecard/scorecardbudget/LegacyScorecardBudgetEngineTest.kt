package com.acme.transport.safety.scorecard.scorecardbudget

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class LegacyScorecardBudgetEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultLegacyScorecardBudgetEngine(newRegistry())
        val item = newLegacyScorecardBudget("code-303", "name-563")
        val result = engine.evaluate(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
