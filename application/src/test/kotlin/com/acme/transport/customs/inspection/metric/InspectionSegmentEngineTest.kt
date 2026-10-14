package com.acme.transport.customs.inspection.metric

import kotlin.test.Test
import kotlin.test.assertTrue

class InspectionSegmentEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultInspectionSegmentEngine()
        val item = newInspectionSegment("code-442", "label-410")
        val result = engine.price(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
