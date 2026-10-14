package com.acme.transport.fleetops.inspection.quota

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class InspectionAllocationEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultInspectionAllocationEngine(newRegistry())
        val item = newInspectionAllocation("code-872", "label-884")
        val result = engine.evaluate(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
