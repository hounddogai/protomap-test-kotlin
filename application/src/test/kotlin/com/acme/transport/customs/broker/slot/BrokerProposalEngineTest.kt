package com.acme.transport.customs.broker.slot

import kotlin.test.Test
import kotlin.test.assertTrue

class BrokerProposalEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultBrokerProposalEngine()
        val item = newBrokerProposal("code-605", "title-440")
        val result = engine.run(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
