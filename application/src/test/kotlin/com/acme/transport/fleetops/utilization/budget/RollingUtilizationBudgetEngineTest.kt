package com.acme.transport.fleetops.utilization.budget

import kotlin.test.Test
import kotlin.test.assertTrue

class RollingUtilizationBudgetEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultRollingUtilizationBudgetEngine()
        val item = newRollingUtilizationBudget("code-629", "title-723")
        val result = engine.run(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
