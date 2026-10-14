package com.acme.transport.network.node.bundle

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class NodeRollupEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultNodeRollupEngine(newRegistry())
        val item = newNodeRollup("code-384", "label-536")
        val result = engine.run(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
