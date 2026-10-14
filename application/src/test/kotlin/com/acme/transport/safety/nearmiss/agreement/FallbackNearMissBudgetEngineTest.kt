package com.acme.transport.safety.nearmiss.agreement

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class FallbackNearMissBudgetEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultFallbackNearMissBudgetEngine(newRegistry())
        val item = newFallbackNearMissBudget("code-54", "label-626")
        val result = engine.run(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
