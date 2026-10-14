package com.acme.transport.network.node.overrideitem

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class BatchedNodePolicyEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultBatchedNodePolicyEngine(newRegistry())
        val item = newBatchedNodePolicy("code-311", "label-751")
        val result = engine.price(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
