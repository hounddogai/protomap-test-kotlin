package com.acme.transport.terminal.dwell.snapshot

import kotlin.test.Test
import kotlin.test.assertTrue

class GlobalDwellBudgetEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultGlobalDwellBudgetEngine()
        val item = newGlobalDwellBudget("code-937", "label-480")
        val result = engine.compute(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
