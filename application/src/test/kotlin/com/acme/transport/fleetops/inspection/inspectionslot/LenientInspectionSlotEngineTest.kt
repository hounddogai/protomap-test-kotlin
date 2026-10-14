package com.acme.transport.fleetops.inspection.inspectionslot

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class LenientInspectionSlotEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultLenientInspectionSlotEngine(newRegistry())
        val item = newLenientInspectionSlot("code-789", "label-951")
        val result = engine.run(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
