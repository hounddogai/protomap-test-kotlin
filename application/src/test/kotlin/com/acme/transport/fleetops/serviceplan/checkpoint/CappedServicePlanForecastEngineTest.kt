package com.acme.transport.fleetops.serviceplan.checkpoint

import kotlin.test.Test
import kotlin.test.assertTrue

class CappedServicePlanForecastEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultCappedServicePlanForecastEngine()
        val item = newCappedServicePlanForecast("code-531", "title-607")
        val result = engine.price(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
