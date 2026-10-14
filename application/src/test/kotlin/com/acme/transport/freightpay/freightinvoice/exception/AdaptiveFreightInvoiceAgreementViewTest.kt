package com.acme.transport.freightpay.freightinvoice.exception

import kotlin.test.Test
import kotlin.test.assertEquals

class AdaptiveFreightInvoiceAgreementViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newAdaptiveFreightInvoiceAgreement("code-385", "title-127")
        val view = toAdaptiveFreightInvoiceAgreementView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toAdaptiveFreightInvoiceAgreementViews(mutableListOf<AdaptiveFreightInvoiceAgreement>(item, item)).size, "views")
    }
}
