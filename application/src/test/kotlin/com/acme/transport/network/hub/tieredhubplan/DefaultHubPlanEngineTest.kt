package com.acme.transport.network.hub.tieredhubplan

import kotlin.test.Test
import kotlin.test.assertTrue

class DefaultHubPlanEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultDefaultHubPlanEngine()
        val item = newDefaultHubPlan("code-799", "label-712")
        val result = engine.price(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
