package com.acme.transport.customs.clearance.quota

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class AdaptiveClearanceBudgetEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultAdaptiveClearanceBudgetEngine(newRegistry())
        val item = newAdaptiveClearanceBudget("code-141", "label-123")
        val result = engine.evaluate(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
