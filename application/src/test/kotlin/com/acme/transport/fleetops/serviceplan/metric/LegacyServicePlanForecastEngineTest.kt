package com.acme.transport.fleetops.serviceplan.metric

import kotlin.test.Test
import kotlin.test.assertTrue

class LegacyServicePlanForecastEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultLegacyServicePlanForecastEngine()
        val item = newLegacyServicePlanForecast("code-548", "title-872")
        val result = engine.evaluate(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
