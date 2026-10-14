package com.acme.transport.fleetops.inspection.policy

import kotlin.test.Test
import kotlin.test.assertTrue

class InspectionPolicyEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultInspectionPolicyEngine()
        val item = newInspectionPolicy("code-945", "label-606")
        val result = engine.evaluate(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
