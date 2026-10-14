package com.acme.transport.network.node.metric

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class NodeAuditEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultNodeAuditEngine(newRegistry())
        val item = newNodeAudit("code-14", "label-613")
        val result = engine.price(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
