package com.acme.transport.fleetops.serviceplan.request

import kotlin.test.Test
import kotlin.test.assertTrue

class ServicePlanQuotaEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultServicePlanQuotaEngine()
        val item = newServicePlanQuota("code-548", "label-33")
        val result = engine.compute(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
