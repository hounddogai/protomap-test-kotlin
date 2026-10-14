package com.acme.transport.fleetops.serviceplan.tier

import kotlin.test.Test
import kotlin.test.assertTrue

class ServicePlanTierEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultServicePlanTierEngine()
        val item = newServicePlanTier("code-169", "name-669")
        val result = engine.compute(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
