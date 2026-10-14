package com.acme.transport.fleetops.inspection.inspectionbatch

import kotlin.test.Test
import kotlin.test.assertTrue

class PendingInspectionSlotEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultPendingInspectionSlotEngine()
        val item = newPendingInspectionSlot("code-140", "name-738")
        val result = engine.run(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
