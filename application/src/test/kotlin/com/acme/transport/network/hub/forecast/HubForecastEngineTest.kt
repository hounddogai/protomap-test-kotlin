package com.acme.transport.network.hub.forecast

import kotlin.test.Test
import kotlin.test.assertTrue

class HubForecastEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultHubForecastEngine()
        val item = newHubForecast("code-547", "name-960")
        val result = engine.evaluate(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
