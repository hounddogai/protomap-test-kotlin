package com.acme.transport.terminal.crossdock.rollup

import kotlin.test.Test
import kotlin.test.assertTrue

class CrossDockBudgetEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultCrossDockBudgetEngine()
        val item = newCrossDockBudget("code-199", "name-637")
        val result = engine.run(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
