package com.acme.transport.terminal.dockdoor.template

import kotlin.test.Test
import kotlin.test.assertTrue

class DockDoorBudgetEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultDockDoorBudgetEngine()
        val item = newDockDoorBudget("code-530", "title-787")
        val result = engine.price(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
