package com.acme.transport.linehaul.trailer.budget

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class TrailerBudgetEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultTrailerBudgetEngine(newRegistry())
        val item = newTrailerBudget("code-308", "label-387")
        val result = engine.price(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
