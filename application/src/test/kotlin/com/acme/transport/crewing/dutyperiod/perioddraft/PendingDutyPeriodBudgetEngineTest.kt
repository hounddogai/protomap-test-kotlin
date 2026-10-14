package com.acme.transport.crewing.dutyperiod.perioddraft

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class PendingDutyPeriodBudgetEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultPendingDutyPeriodBudgetEngine(newRegistry())
        val item = newPendingDutyPeriodBudget("code-871", "name-163")
        val result = engine.compute(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
