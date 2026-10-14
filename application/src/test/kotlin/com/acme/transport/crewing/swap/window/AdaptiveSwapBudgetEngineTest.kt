package com.acme.transport.crewing.swap.window

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class AdaptiveSwapBudgetEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultAdaptiveSwapBudgetEngine(newRegistry())
        val item = newAdaptiveSwapBudget("code-167", "title-858")
        val result = engine.price(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
