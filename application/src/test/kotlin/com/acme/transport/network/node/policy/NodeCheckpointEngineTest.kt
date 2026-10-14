package com.acme.transport.network.node.policy

import kotlin.test.Test
import kotlin.test.assertTrue

class NodeCheckpointEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultNodeCheckpointEngine()
        val item = newNodeCheckpoint("code-447", "name-738")
        val result = engine.evaluate(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
