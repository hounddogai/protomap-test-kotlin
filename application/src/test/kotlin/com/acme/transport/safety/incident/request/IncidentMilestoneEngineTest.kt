package com.acme.transport.safety.incident.request

import kotlin.test.Test
import kotlin.test.assertTrue

class IncidentMilestoneEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultIncidentMilestoneEngine()
        val item = newIncidentMilestone("code-287", "name-814")
        val result = engine.compute(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
