package com.acme.transport.fleetops.serviceplan.planprofile

import kotlin.test.Test
import kotlin.test.assertTrue

class LegacyServicePlanMetricEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultLegacyServicePlanMetricEngine()
        val item = newLegacyServicePlanMetric("code-421", "label-206")
        val result = engine.price(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
