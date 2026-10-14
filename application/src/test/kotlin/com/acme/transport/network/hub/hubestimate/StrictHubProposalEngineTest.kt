package com.acme.transport.network.hub.hubestimate

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class StrictHubProposalEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultStrictHubProposalEngine(newRegistry())
        val item = newStrictHubProposal("code-727", "title-888")
        val result = engine.run(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
