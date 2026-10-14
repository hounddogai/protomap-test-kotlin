package com.acme.transport.network.node.policy

import kotlin.test.Test
import kotlin.test.assertTrue

class NodeWindowEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultNodeWindowEngine()
        val item = newNodeWindow("code-503", "name-829")
        val result = engine.price(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
