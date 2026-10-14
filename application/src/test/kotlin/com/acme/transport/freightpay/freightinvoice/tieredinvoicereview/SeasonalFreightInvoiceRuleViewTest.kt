package com.acme.transport.freightpay.freightinvoice.tieredinvoicereview

import kotlin.test.Test
import kotlin.test.assertEquals

class SeasonalFreightInvoiceRuleViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newSeasonalFreightInvoiceRule("code-157", "name-248")
        val view = toSeasonalFreightInvoiceRuleView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toSeasonalFreightInvoiceRuleViews(mutableListOf<SeasonalFreightInvoiceRule>(item, item)).size, "views")
    }
}
