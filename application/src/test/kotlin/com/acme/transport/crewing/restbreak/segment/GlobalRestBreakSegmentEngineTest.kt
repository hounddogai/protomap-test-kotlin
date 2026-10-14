package com.acme.transport.crewing.restbreak.segment

import kotlin.test.Test
import kotlin.test.assertTrue

class GlobalRestBreakSegmentEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultGlobalRestBreakSegmentEngine()
        val item = newGlobalRestBreakSegment("code-234", "name-182")
        val result = engine.price(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
