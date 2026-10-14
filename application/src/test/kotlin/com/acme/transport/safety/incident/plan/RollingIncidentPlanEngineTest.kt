package com.acme.transport.safety.incident.plan

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class RollingIncidentPlanEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultRollingIncidentPlanEngine(newRegistry())
        val item = newRollingIncidentPlan("code-98", "name-136")
        val result = engine.compute(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
