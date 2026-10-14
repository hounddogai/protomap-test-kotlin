package com.acme.transport.crewing.swap.rule

import kotlin.test.Test
import kotlin.test.assertTrue

class SwapSegmentEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultSwapSegmentEngine()
        val item = newSwapSegment("code-148", "title-231")
        val result = engine.evaluate(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
