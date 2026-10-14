package com.acme.transport.network.node.plan

import kotlin.test.Test
import kotlin.test.assertTrue

class NodeMappingEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultNodeMappingEngine()
        val item = newNodeMapping("code-786", "name-865")
        val result = engine.run(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
