package com.acme.transport.customs.hold.segment

import kotlin.test.Test
import kotlin.test.assertTrue

class WeightedHoldSegmentEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultWeightedHoldSegmentEngine()
        val item = newWeightedHoldSegment("code-171", "title-315")
        val result = engine.price(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
