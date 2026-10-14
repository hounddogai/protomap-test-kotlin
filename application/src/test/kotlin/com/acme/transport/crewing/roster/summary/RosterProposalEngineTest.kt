package com.acme.transport.crewing.roster.summary

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class RosterProposalEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultRosterProposalEngine(newRegistry())
        val item = newRosterProposal("code-657", "title-535")
        val result = engine.price(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
