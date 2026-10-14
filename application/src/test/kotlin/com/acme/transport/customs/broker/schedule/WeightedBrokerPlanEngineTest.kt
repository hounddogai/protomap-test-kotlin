package com.acme.transport.customs.broker.schedule

import kotlin.test.Test
import kotlin.test.assertTrue

class WeightedBrokerPlanEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultWeightedBrokerPlanEngine()
        val item = newWeightedBrokerPlan("code-452", "name-399")
        val result = engine.price(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
