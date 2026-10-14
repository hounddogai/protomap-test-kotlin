package com.acme.transport.freightpay.freightinvoice.digest

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class SeasonalFreightInvoiceProposalTest {
    @Test
    fun `starts in draft`() {
        val item = newSeasonalFreightInvoiceProposal("code-249", "label-921")
        assertEquals(SeasonalFreightInvoiceProposalStatus.DRAFT, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to suspended`() {
        val item = newSeasonalFreightInvoiceProposal("code-249", "label-921")
        try {
            item.moveTo(SeasonalFreightInvoiceProposalStatus.SUSPENDED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(SeasonalFreightInvoiceProposalStatus.SUSPENDED, item.status, "status")
    }

    @Test
    fun `rejects moves from closed`() {
        val item = newSeasonalFreightInvoiceProposal("code-249", "label-921")
        item.status = SeasonalFreightInvoiceProposalStatus.CLOSED
        assertFails("move") { item.moveTo(SeasonalFreightInvoiceProposalStatus.DRAFT) }
    }
}
