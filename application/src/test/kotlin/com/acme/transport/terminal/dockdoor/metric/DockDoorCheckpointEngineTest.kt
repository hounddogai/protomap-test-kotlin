package com.acme.transport.terminal.dockdoor.metric

import kotlin.test.Test
import kotlin.test.assertTrue

class DockDoorCheckpointEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultDockDoorCheckpointEngine()
        val item = newDockDoorCheckpoint("code-618", "name-125")
        val result = engine.compute(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
