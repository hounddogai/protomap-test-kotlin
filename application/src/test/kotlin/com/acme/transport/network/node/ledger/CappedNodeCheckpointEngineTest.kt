package com.acme.transport.network.node.ledger

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class CappedNodeCheckpointEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultCappedNodeCheckpointEngine(newRegistry())
        val item = newCappedNodeCheckpoint("code-42", "title-171")
        val result = engine.compute(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
