package com.acme.transport.fleetops.serviceplan.batchedplanrollup

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class ServicePlanPolicyEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultServicePlanPolicyEngine(newRegistry())
        val item = newServicePlanPolicy("code-135", "label-173")
        val result = engine.evaluate(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
