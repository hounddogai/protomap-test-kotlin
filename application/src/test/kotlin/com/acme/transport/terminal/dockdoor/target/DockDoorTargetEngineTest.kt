package com.acme.transport.terminal.dockdoor.target

import kotlin.test.Test
import kotlin.test.assertTrue

class DockDoorTargetEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultDockDoorTargetEngine()
        val item = newDockDoorTarget("code-683", "name-995")
        val result = engine.run(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
