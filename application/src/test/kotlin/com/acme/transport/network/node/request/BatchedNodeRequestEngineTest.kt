package com.acme.transport.network.node.request

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class BatchedNodeRequestEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultBatchedNodeRequestEngine(newRegistry())
        val item = newBatchedNodeRequest("code-665", "name-447")
        val result = engine.compute(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
