package com.acme.transport.safety.nearmiss.budget

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class NearMissBudgetEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultNearMissBudgetEngine(newRegistry())
        val item = newNearMissBudget("code-35", "name-10")
        val result = engine.evaluate(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
