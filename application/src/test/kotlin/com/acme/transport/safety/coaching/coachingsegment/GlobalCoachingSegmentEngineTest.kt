package com.acme.transport.safety.coaching.coachingsegment

import kotlin.test.Test
import kotlin.test.assertTrue

class GlobalCoachingSegmentEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultGlobalCoachingSegmentEngine()
        val item = newGlobalCoachingSegment("code-871", "name-947")
        val result = engine.compute(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
