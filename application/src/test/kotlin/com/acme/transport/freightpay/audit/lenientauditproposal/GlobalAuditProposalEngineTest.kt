package com.acme.transport.freightpay.audit.lenientauditproposal

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class GlobalAuditProposalEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultGlobalAuditProposalEngine(newRegistry())
        val item = newGlobalAuditProposal("code-434", "title-543")
        val result = engine.compute(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
