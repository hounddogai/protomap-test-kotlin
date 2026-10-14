package com.acme.transport.fleetops.serviceplan.claim

import kotlin.test.Test
import kotlin.test.assertTrue

class ServicePlanAssignmentEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultServicePlanAssignmentEngine()
        val item = newServicePlanAssignment("code-122", "title-425")
        val result = engine.price(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
