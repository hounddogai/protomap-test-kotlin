package com.acme.transport.network.node.request

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class NodeBaselineEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultNodeBaselineEngine(newRegistry())
        val item = newNodeBaseline("code-214", "name-746")
        val result = engine.run(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
