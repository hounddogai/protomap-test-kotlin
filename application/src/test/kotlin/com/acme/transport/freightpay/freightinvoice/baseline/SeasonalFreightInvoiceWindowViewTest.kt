package com.acme.transport.freightpay.freightinvoice.baseline

import kotlin.test.Test
import kotlin.test.assertEquals

class SeasonalFreightInvoiceWindowViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newSeasonalFreightInvoiceWindow("code-897", "name-255")
        val view = toSeasonalFreightInvoiceWindowView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toSeasonalFreightInvoiceWindowViews(mutableListOf<SeasonalFreightInvoiceWindow>(item, item)).size, "views")
    }
}
