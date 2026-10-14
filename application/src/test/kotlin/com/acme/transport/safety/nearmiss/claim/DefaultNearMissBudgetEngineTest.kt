package com.acme.transport.safety.nearmiss.claim

import kotlin.test.Test
import kotlin.test.assertTrue

class DefaultNearMissBudgetEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultDefaultNearMissBudgetEngine()
        val item = newDefaultNearMissBudget("code-63", "name-195")
        val result = engine.run(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
