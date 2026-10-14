package com.acme.transport.network.node.cycle

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class NodeCycleEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultNodeCycleEngine(newRegistry())
        val item = newNodeCycle("code-348", "title-895")
        val result = engine.compute(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
