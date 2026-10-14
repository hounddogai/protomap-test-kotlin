package com.acme.transport.fleetops.serviceplan.planpreference

import kotlin.test.Test
import kotlin.test.assertTrue

class ServicePlanRuleEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultServicePlanRuleEngine()
        val item = newServicePlanRule("code-41", "label-731")
        val result = engine.price(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
