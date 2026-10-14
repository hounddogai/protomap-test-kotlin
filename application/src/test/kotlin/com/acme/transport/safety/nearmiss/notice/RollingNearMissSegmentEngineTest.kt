package com.acme.transport.safety.nearmiss.notice

import kotlin.test.Test
import kotlin.test.assertTrue

class RollingNearMissSegmentEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultRollingNearMissSegmentEngine()
        val item = newRollingNearMissSegment("code-791", "label-47")
        val result = engine.price(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
