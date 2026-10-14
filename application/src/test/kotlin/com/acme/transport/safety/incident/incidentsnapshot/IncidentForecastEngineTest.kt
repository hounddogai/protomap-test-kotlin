package com.acme.transport.safety.incident.incidentsnapshot

import kotlin.test.Test
import kotlin.test.assertTrue

class IncidentForecastEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultIncidentForecastEngine()
        val item = newIncidentForecast("code-196", "title-535")
        val result = engine.evaluate(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
