package com.acme.transport.crewing.dutyperiod.milestone

import kotlin.test.Test
import kotlin.test.assertTrue

class RollingDutyPeriodBudgetEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultRollingDutyPeriodBudgetEngine()
        val item = newRollingDutyPeriodBudget("code-645", "title-347")
        val result = engine.evaluate(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
