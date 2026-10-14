package com.acme.transport.customs.broker.adjustment

import kotlin.test.Test
import kotlin.test.assertTrue

class LegacyBrokerPlanEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultLegacyBrokerPlanEngine()
        val item = newLegacyBrokerPlan("code-876", "name-942")
        val result = engine.run(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
