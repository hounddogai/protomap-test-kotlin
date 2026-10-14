package com.acme.transport.safety.scorecard.scorecardgroup

import kotlin.test.Test
import kotlin.test.assertTrue

class AdaptiveScorecardSegmentEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultAdaptiveScorecardSegmentEngine()
        val item = newAdaptiveScorecardSegment("code-904", "title-718")
        val result = engine.compute(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
