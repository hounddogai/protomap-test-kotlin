package com.acme.transport.freightpay.freightinvoice.proposal

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class FreightInvoiceProposalTest {
    @Test
    fun `starts in new`() {
        val item = newFreightInvoiceProposal("code-76", "label-81")
        assertEquals(FreightInvoiceProposalStatus.NEW, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to queued`() {
        val item = newFreightInvoiceProposal("code-76", "label-81")
        try {
            item.moveTo(FreightInvoiceProposalStatus.QUEUED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(FreightInvoiceProposalStatus.QUEUED, item.status, "status")
    }

    @Test
    fun `rejects moves from failed`() {
        val item = newFreightInvoiceProposal("code-76", "label-81")
        item.status = FreightInvoiceProposalStatus.FAILED
        assertFails("move") { item.moveTo(FreightInvoiceProposalStatus.NEW) }
    }
}
