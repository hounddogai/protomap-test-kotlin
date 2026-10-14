package com.acme.transport.safety.violation.metric

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class RollingViolationBudgetEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultRollingViolationBudgetEngine(newRegistry())
        val item = newRollingViolationBudget("code-780", "title-20")
        val result = engine.run(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
