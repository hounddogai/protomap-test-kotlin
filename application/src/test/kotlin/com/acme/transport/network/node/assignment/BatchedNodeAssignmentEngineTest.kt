package com.acme.transport.network.node.assignment

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class BatchedNodeAssignmentEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultBatchedNodeAssignmentEngine(newRegistry())
        val item = newBatchedNodeAssignment("code-833", "title-20")
        val result = engine.evaluate(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
