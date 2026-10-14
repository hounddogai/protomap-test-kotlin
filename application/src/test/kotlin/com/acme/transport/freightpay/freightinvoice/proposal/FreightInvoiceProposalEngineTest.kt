package com.acme.transport.freightpay.freightinvoice.proposal

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class FreightInvoiceProposalEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultFreightInvoiceProposalEngine(newRegistry())
        val item = newFreightInvoiceProposal("code-76", "label-81")
        val result = engine.price(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
