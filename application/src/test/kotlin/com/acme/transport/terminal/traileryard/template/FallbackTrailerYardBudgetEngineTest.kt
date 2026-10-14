package com.acme.transport.terminal.traileryard.template

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class FallbackTrailerYardBudgetEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultFallbackTrailerYardBudgetEngine(newRegistry())
        val item = newFallbackTrailerYardBudget("code-195", "name-615")
        val result = engine.compute(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
