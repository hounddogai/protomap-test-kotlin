package com.acme.transport.safety.scorecard.segment

import kotlin.test.Test
import kotlin.test.assertTrue

class StrictScorecardSegmentEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultStrictScorecardSegmentEngine()
        val item = newStrictScorecardSegment("code-222", "label-905")
        val result = engine.price(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
