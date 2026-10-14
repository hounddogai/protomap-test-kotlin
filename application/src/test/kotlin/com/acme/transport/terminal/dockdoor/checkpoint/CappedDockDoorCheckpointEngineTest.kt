package com.acme.transport.terminal.dockdoor.checkpoint

import kotlin.test.Test
import kotlin.test.assertTrue

class CappedDockDoorCheckpointEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultCappedDockDoorCheckpointEngine()
        val item = newCappedDockDoorCheckpoint("code-331", "title-457")
        val result = engine.price(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
