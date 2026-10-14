package com.acme.transport.network.node.policy

import kotlin.test.Test
import kotlin.test.assertTrue

class NodePolicyEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultNodePolicyEngine()
        val item = newNodePolicy("code-927", "title-559")
        val result = engine.run(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
