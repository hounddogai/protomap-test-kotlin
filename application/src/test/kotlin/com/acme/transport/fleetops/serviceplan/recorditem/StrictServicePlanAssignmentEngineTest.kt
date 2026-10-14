package com.acme.transport.fleetops.serviceplan.recorditem

import kotlin.test.Test
import kotlin.test.assertTrue

class StrictServicePlanAssignmentEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultStrictServicePlanAssignmentEngine()
        val item = newStrictServicePlanAssignment("code-973", "title-474")
        val result = engine.compute(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
