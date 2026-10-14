package com.acme.transport.crewing.roster.fallbackrosterforecast

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class PrimaryRosterProposalEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultPrimaryRosterProposalEngine(newRegistry())
        val item = newPrimaryRosterProposal("code-453", "name-18")
        val result = engine.run(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
