package com.acme.transport.lastmile.parcel.reservation

import kotlin.test.Test
import kotlin.test.assertTrue

class ScheduledParcelBudgetEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultScheduledParcelBudgetEngine()
        val item = newScheduledParcelBudget("code-600", "title-203")
        val result = engine.price(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
