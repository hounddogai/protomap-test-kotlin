package com.acme.transport.crewing.swap.entry

import kotlin.test.Test
import kotlin.test.assertTrue

class WeightedSwapSegmentEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultWeightedSwapSegmentEngine()
        val item = newWeightedSwapSegment("code-976", "name-175")
        val result = engine.price(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
