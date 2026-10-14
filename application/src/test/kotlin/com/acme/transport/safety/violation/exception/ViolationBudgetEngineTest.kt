package com.acme.transport.safety.violation.exception

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class ViolationBudgetEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultViolationBudgetEngine(newRegistry())
        val item = newViolationBudget("code-67", "name-92")
        val result = engine.price(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
