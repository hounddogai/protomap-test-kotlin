package com.acme.transport.terminal.dockdoor.ledger

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class DockDoorTemplateEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultDockDoorTemplateEngine(newRegistry())
        val item = newDockDoorTemplate("code-754", "label-731")
        val result = engine.compute(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
