package com.acme.transport.terminal.dockdoor.doorscore

import kotlin.test.Test
import kotlin.test.assertTrue

class DockDoorDraftEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultDockDoorDraftEngine()
        val item = newDockDoorDraft("code-225", "name-549")
        val result = engine.compute(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
