package com.acme.transport.freightpay.freightinvoice.invoiceentry

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class WeightedFreightInvoiceProposalTest {
    @Test
    fun `starts in requested`() {
        val item = newWeightedFreightInvoiceProposal("code-723", "title-968")
        assertEquals(WeightedFreightInvoiceProposalStatus.REQUESTED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to granted`() {
        val item = newWeightedFreightInvoiceProposal("code-723", "title-968")
        try {
            item.moveTo(WeightedFreightInvoiceProposalStatus.GRANTED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(WeightedFreightInvoiceProposalStatus.GRANTED, item.status, "status")
    }

    @Test
    fun `rejects moves from expired`() {
        val item = newWeightedFreightInvoiceProposal("code-723", "title-968")
        item.status = WeightedFreightInvoiceProposalStatus.EXPIRED
        assertFails("move") { item.moveTo(WeightedFreightInvoiceProposalStatus.REQUESTED) }
    }
}
