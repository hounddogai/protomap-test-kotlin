package com.acme.transport.fleetops.serviceplan.summary

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class ServicePlanForecastEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultServicePlanForecastEngine(newRegistry())
        val item = newServicePlanForecast("code-744", "label-44")
        val result = engine.run(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
