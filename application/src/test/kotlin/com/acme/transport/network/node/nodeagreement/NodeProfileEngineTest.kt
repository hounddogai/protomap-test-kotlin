package com.acme.transport.network.node.nodeagreement

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class NodeProfileEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultNodeProfileEngine(newRegistry())
        val item = newNodeProfile("code-245", "title-483")
        val result = engine.run(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
