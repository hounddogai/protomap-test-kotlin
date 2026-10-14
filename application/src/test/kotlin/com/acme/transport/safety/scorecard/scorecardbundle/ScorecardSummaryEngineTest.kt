package com.acme.transport.safety.scorecard.scorecardbundle

import kotlin.test.Test
import kotlin.test.assertTrue

class ScorecardSummaryEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultScorecardSummaryEngine()
        val item = newScorecardSummary("code-580", "name-967")
        val result = engine.compute(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
