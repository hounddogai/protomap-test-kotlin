package com.acme.transport.network.hub.plan

import kotlin.test.Test
import kotlin.test.assertTrue

class HubPlanEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultHubPlanEngine()
        val item = newHubPlan("code-650", "name-229")
        val result = engine.price(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
