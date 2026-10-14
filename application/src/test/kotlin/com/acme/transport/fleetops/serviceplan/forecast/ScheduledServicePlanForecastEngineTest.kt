package com.acme.transport.fleetops.serviceplan.forecast

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class ScheduledServicePlanForecastEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultScheduledServicePlanForecastEngine(newRegistry())
        val item = newScheduledServicePlanForecast("code-484", "label-30")
        val result = engine.run(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
