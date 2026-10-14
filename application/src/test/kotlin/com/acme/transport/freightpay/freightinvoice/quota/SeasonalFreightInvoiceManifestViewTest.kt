package com.acme.transport.freightpay.freightinvoice.quota

import kotlin.test.Test
import kotlin.test.assertEquals

class SeasonalFreightInvoiceManifestViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newSeasonalFreightInvoiceManifest("code-516", "label-989")
        val view = toSeasonalFreightInvoiceManifestView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toSeasonalFreightInvoiceManifestViews(mutableListOf<SeasonalFreightInvoiceManifest>(item, item)).size, "views")
    }
}
