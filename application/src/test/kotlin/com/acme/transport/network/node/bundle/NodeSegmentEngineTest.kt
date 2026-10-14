package com.acme.transport.network.node.bundle

import kotlin.test.Test
import kotlin.test.assertTrue

class NodeSegmentEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultNodeSegmentEngine()
        val item = newNodeSegment("code-538", "label-294")
        val result = engine.evaluate(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
