package com.acme.transport.crewing.swap.mapping

import kotlin.test.Test
import kotlin.test.assertTrue

class SeasonalSwapSegmentEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultSeasonalSwapSegmentEngine()
        val item = newSeasonalSwapSegment("code-42", "name-43")
        val result = engine.price(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
