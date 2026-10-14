package com.acme.transport.terminal.dockdoor.checkpoint

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class BatchedDockDoorBaselineEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultBatchedDockDoorBaselineEngine(newRegistry())
        val item = newBatchedDockDoorBaseline("code-128", "title-135")
        val result = engine.compute(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
