package com.acme.transport.fleetops.serviceplan.threshold

import kotlin.test.Test
import kotlin.test.assertTrue

class SeasonalServicePlanBudgetEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultSeasonalServicePlanBudgetEngine()
        val item = newSeasonalServicePlanBudget("code-983", "name-49")
        val result = engine.price(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
