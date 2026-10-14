package com.acme.transport.network.node.nodescore

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class NodeRuleEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultNodeRuleEngine(newRegistry())
        val item = newNodeRule("code-887", "label-22")
        val result = engine.run(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
