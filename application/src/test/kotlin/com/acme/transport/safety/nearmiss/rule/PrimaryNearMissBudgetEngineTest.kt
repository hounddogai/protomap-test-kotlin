package com.acme.transport.safety.nearmiss.rule

import kotlin.test.Test
import kotlin.test.assertTrue

class PrimaryNearMissBudgetEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultPrimaryNearMissBudgetEngine()
        val item = newPrimaryNearMissBudget("code-348", "title-571")
        val result = engine.compute(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
