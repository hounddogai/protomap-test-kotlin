package com.acme.transport.terminal.dockdoor.window

import kotlin.test.Test
import kotlin.test.assertTrue

class DockDoorBaselineEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultDockDoorBaselineEngine()
        val item = newDockDoorBaseline("code-557", "label-957")
        val result = engine.price(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
