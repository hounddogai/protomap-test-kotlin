package com.acme.transport.fleetops.inspection.segment

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class InspectionSegmentEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultInspectionSegmentEngine(newRegistry())
        val item = newInspectionSegment("code-358", "label-852")
        val result = engine.price(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
