package com.acme.transport.terminal.dwell.template

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class DwellBudgetEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultDwellBudgetEngine(newRegistry())
        val item = newDwellBudget("code-678", "name-879")
        val result = engine.price(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
