package com.acme.transport.network.node.allocation

import kotlin.test.Test
import kotlin.test.assertTrue

class NodeAdjustmentEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultNodeAdjustmentEngine()
        val item = newNodeAdjustment("code-740", "title-768")
        val result = engine.run(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
