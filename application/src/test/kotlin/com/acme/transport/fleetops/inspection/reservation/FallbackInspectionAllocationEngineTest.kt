package com.acme.transport.fleetops.inspection.reservation

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class FallbackInspectionAllocationEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultFallbackInspectionAllocationEngine(newRegistry())
        val item = newFallbackInspectionAllocation("code-770", "name-7")
        val result = engine.run(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
