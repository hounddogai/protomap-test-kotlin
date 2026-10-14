package com.acme.transport.freightpay.freightinvoice.invoiceentry

import kotlin.test.Test
import kotlin.test.assertEquals

class WeightedFreightInvoiceProposalViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newWeightedFreightInvoiceProposal("code-723", "title-968")
        val view = toWeightedFreightInvoiceProposalView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toWeightedFreightInvoiceProposalViews(mutableListOf<WeightedFreightInvoiceProposal>(item, item)).size, "views")
    }
}
