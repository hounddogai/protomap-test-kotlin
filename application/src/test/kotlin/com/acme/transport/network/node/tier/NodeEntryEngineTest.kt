package com.acme.transport.network.node.tier

import kotlin.test.Test
import kotlin.test.assertTrue

class NodeEntryEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultNodeEntryEngine()
        val item = newNodeEntry("code-185", "name-905")
        val result = engine.compute(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
