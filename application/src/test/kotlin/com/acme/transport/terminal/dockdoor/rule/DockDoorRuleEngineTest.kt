package com.acme.transport.terminal.dockdoor.rule

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class DockDoorRuleEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultDockDoorRuleEngine(newRegistry())
        val item = newDockDoorRule("code-377", "name-185")
        val result = engine.compute(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
