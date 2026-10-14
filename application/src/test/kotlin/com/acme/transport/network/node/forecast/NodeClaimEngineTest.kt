package com.acme.transport.network.node.forecast

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class NodeClaimEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultNodeClaimEngine(newRegistry())
        val item = newNodeClaim("code-883", "name-99")
        val result = engine.price(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
