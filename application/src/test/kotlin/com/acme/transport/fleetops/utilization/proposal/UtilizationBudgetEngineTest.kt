package com.acme.transport.fleetops.utilization.proposal

import kotlin.test.Test
import kotlin.test.assertTrue

class UtilizationBudgetEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultUtilizationBudgetEngine()
        val item = newUtilizationBudget("code-116", "name-337")
        val result = engine.evaluate(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
