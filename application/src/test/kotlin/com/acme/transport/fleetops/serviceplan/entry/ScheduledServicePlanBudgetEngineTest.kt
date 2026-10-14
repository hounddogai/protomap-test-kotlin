package com.acme.transport.fleetops.serviceplan.entry

import kotlin.test.Test
import kotlin.test.assertTrue

class ScheduledServicePlanBudgetEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultScheduledServicePlanBudgetEngine()
        val item = newScheduledServicePlanBudget("code-710", "name-700")
        val result = engine.evaluate(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
