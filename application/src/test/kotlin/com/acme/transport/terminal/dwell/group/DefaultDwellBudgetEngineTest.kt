package com.acme.transport.terminal.dwell.group

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class DefaultDwellBudgetEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultDefaultDwellBudgetEngine(newRegistry())
        val item = newDefaultDwellBudget("code-960", "title-373")
        val result = engine.price(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
