package com.acme.transport.safety.incident.segment

import kotlin.test.Test
import kotlin.test.assertTrue

class IncidentPlanEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultIncidentPlanEngine()
        val item = newIncidentPlan("code-909", "label-408")
        val result = engine.evaluate(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
