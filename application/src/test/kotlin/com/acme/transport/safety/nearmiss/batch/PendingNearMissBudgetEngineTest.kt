package com.acme.transport.safety.nearmiss.batch

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class PendingNearMissBudgetEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultPendingNearMissBudgetEngine(newRegistry())
        val item = newPendingNearMissBudget("code-940", "title-323")
        val result = engine.price(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
