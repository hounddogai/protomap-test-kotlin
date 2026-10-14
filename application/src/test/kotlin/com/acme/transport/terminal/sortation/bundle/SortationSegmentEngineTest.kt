package com.acme.transport.terminal.sortation.bundle

import kotlin.test.Test
import kotlin.test.assertTrue

class SortationSegmentEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultSortationSegmentEngine()
        val item = newSortationSegment("code-982", "label-107")
        val result = engine.price(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
