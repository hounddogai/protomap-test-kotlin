package com.acme.transport.customs.inspection.inspectionrule

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class FallbackInspectionSegmentEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultFallbackInspectionSegmentEngine(newRegistry())
        val item = newFallbackInspectionSegment("code-249", "title-964")
        val result = engine.compute(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
