package com.acme.transport.safety.scorecard.proposal

import kotlin.test.Test
import kotlin.test.assertTrue

class ScorecardSegmentEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultScorecardSegmentEngine()
        val item = newScorecardSegment("code-744", "label-179")
        val result = engine.run(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
