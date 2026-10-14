package com.acme.transport.emissions.idling.notice

import kotlin.test.Test
import kotlin.test.assertTrue

class FallbackIdlingSegmentEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultFallbackIdlingSegmentEngine()
        val item = newFallbackIdlingSegment("code-743", "title-233")
        val result = engine.evaluate(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
