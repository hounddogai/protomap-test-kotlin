package com.acme.transport.lastmile.locker.preference

import kotlin.test.Test
import kotlin.test.assertTrue

class LockerBudgetEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultLockerBudgetEngine()
        val item = newLockerBudget("code-329", "name-92")
        val result = engine.price(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
