package com.acme.transport.network.node.template

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class TieredNodeProposalEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultTieredNodeProposalEngine(newRegistry())
        val item = newTieredNodeProposal("code-273", "label-760")
        val result = engine.evaluate(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
