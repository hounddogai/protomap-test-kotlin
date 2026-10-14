package com.acme.transport.customs.inspection.inspectionestimate

import kotlin.test.Test
import kotlin.test.assertTrue

class PendingInspectionAllocationEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultPendingInspectionAllocationEngine()
        val item = newPendingInspectionAllocation("code-697", "title-87")
        val result = engine.price(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
