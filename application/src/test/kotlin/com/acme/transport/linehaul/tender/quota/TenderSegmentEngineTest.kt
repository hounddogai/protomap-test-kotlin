package com.acme.transport.linehaul.tender.quota

import kotlin.test.Test
import kotlin.test.assertTrue

class TenderSegmentEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultTenderSegmentEngine()
        val item = newTenderSegment("code-545", "label-763")
        val result = engine.compute(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
