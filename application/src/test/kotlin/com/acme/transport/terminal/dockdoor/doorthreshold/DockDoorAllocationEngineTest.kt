package com.acme.transport.terminal.dockdoor.doorthreshold

import kotlin.test.Test
import kotlin.test.assertTrue

class DockDoorAllocationEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultDockDoorAllocationEngine()
        val item = newDockDoorAllocation("code-440", "title-938")
        val result = engine.compute(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
