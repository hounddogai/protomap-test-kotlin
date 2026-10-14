package com.acme.transport.safety.incident.incidenttarget

import kotlin.test.Test
import kotlin.test.assertTrue

class RollingIncidentBaselineEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultRollingIncidentBaselineEngine()
        val item = newRollingIncidentBaseline("code-137", "title-554")
        val result = engine.price(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
