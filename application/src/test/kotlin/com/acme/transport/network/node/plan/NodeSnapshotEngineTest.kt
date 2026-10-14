package com.acme.transport.network.node.plan

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class NodeSnapshotEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultNodeSnapshotEngine(newRegistry())
        val item = newNodeSnapshot("code-185", "label-121")
        val result = engine.run(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
