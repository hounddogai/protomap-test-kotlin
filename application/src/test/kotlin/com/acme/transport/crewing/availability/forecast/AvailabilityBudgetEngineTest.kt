package com.acme.transport.crewing.availability.forecast

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class AvailabilityBudgetEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultAvailabilityBudgetEngine(newRegistry())
        val item = newAvailabilityBudget("code-563", "name-220")
        val result = engine.price(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
