package com.acme.transport.emissions.carbonreport.claim

import kotlin.test.Test
import kotlin.test.assertEquals

class CarbonReportLedgerViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newCarbonReportLedger("code-535", "label-485")
        val view = toCarbonReportLedgerView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toCarbonReportLedgerViews(mutableListOf<CarbonReportLedger>(item, item)).size, "views")
    }
}
