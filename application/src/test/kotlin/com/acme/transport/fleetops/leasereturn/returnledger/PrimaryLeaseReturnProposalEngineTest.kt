package com.acme.transport.fleetops.leasereturn.returnledger

import kotlin.test.Test
import kotlin.test.assertTrue

class PrimaryLeaseReturnProposalEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultPrimaryLeaseReturnProposalEngine()
        val item = newPrimaryLeaseReturnProposal("code-828", "title-698")
        val result = engine.compute(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
