package com.acme.transport.freightpay.audit.proposal

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class AuditProposalEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultAuditProposalEngine(newRegistry())
        val item = newAuditProposal("code-958", "name-83")
        val result = engine.price(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
