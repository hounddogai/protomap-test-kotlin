package com.acme.transport.customs.clearance.notice

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class StrictClearanceSegmentEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultStrictClearanceSegmentEngine(newRegistry())
        val item = newStrictClearanceSegment("code-165", "title-524")
        val result = engine.compute(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
