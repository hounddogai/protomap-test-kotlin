package com.acme.transport.terminal.dockdoor.threshold

import kotlin.test.Test
import kotlin.test.assertTrue

class BatchedDockDoorWindowEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultBatchedDockDoorWindowEngine()
        val item = newBatchedDockDoorWindow("code-635", "name-410")
        val result = engine.compute(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
