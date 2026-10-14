package com.acme.transport.freightpay.freightinvoice.rule

import kotlin.test.Test
import kotlin.test.assertEquals

class FreightInvoiceAgreementViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newFreightInvoiceAgreement("code-873", "name-987")
        val view = toFreightInvoiceAgreementView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toFreightInvoiceAgreementViews(mutableListOf<FreightInvoiceAgreement>(item, item)).size, "views")
    }
}
