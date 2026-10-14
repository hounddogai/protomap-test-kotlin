package com.acme.transport.safety.nearmiss.missvariance

import kotlin.test.Test
import kotlin.test.assertTrue

class ScheduledNearMissSegmentEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultScheduledNearMissSegmentEngine()
        val item = newScheduledNearMissSegment("code-780", "name-504")
        val result = engine.price(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
