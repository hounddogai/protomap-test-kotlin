package com.acme.transport.lastmile.locker.budget

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class ScheduledLockerBudgetEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultScheduledLockerBudgetEngine(newRegistry())
        val item = newScheduledLockerBudget("code-345", "name-52")
        val result = engine.price(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
