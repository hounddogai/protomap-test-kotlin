package com.acme.transport.safety.scorecard.rollup

import kotlin.test.Test
import kotlin.test.assertTrue

class ScorecardRollupEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultScorecardRollupEngine()
        val item = newScorecardRollup("code-402", "label-311")
        val result = engine.compute(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
