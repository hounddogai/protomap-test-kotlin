package com.acme.transport.fleetops.inspection.reservation

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class LenientInspectionSegmentEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultLenientInspectionSegmentEngine(newRegistry())
        val item = newLenientInspectionSegment("code-499", "label-145")
        val result = engine.price(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
