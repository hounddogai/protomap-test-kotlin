package com.acme.transport.customs.inspection.allocation

import kotlin.test.Test
import kotlin.test.assertTrue

class InspectionAllocationEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultInspectionAllocationEngine()
        val item = newInspectionAllocation("code-725", "name-617")
        val result = engine.run(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
