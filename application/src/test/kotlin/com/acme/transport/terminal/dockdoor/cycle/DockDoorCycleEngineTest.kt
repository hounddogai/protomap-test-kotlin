package com.acme.transport.terminal.dockdoor.cycle

import kotlin.test.Test
import kotlin.test.assertTrue

class DockDoorCycleEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultDockDoorCycleEngine()
        val item = newDockDoorCycle("code-388", "title-373")
        val result = engine.evaluate(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
