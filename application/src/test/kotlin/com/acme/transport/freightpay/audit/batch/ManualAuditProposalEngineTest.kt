package com.acme.transport.freightpay.audit.batch

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class ManualAuditProposalEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultManualAuditProposalEngine(newRegistry())
        val item = newManualAuditProposal("code-854", "title-52")
        val result = engine.price(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
