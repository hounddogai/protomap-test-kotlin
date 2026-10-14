package com.acme.transport.freightpay.freightinvoice.digest

import kotlin.test.Test
import kotlin.test.assertEquals

class SeasonalFreightInvoiceProposalViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newSeasonalFreightInvoiceProposal("code-249", "label-921")
        val view = toSeasonalFreightInvoiceProposalView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toSeasonalFreightInvoiceProposalViews(mutableListOf<SeasonalFreightInvoiceProposal>(item, item)).size, "views")
    }
}
