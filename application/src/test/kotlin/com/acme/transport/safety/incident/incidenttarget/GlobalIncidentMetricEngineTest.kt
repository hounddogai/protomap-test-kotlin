package com.acme.transport.safety.incident.incidenttarget

import kotlin.test.Test
import kotlin.test.assertTrue

class GlobalIncidentMetricEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultGlobalIncidentMetricEngine()
        val item = newGlobalIncidentMetric("code-338", "title-70")
        val result = engine.price(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
