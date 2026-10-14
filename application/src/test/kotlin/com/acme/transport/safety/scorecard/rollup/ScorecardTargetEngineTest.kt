package com.acme.transport.safety.scorecard.rollup

import kotlin.test.Test
import kotlin.test.assertTrue

class ScorecardTargetEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultScorecardTargetEngine()
        val item = newScorecardTarget("code-339", "name-978")
        val result = engine.compute(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
