package com.acme.transport.network.transittime.proposal

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class TransitTimeProposalEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultTransitTimeProposalEngine(newRegistry())
        val item = newTransitTimeProposal("code-136", "name-919")
        val result = engine.evaluate(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
