package com.acme.transport.network.node.tier

import kotlin.test.Test
import kotlin.test.assertTrue

class NodeTargetEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultNodeTargetEngine()
        val item = newNodeTarget("code-740", "name-262")
        val result = engine.evaluate(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
