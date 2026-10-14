package com.acme.transport.network.node.cycle

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class NodeSlotEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultNodeSlotEngine(newRegistry())
        val item = newNodeSlot("code-680", "label-824")
        val result = engine.run(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
