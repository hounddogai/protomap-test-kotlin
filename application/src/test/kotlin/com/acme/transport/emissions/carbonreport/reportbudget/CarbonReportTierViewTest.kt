package com.acme.transport.emissions.carbonreport.reportbudget

import kotlin.test.Test
import kotlin.test.assertEquals

class CarbonReportTierViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newCarbonReportTier("code-661", "label-395")
        val view = toCarbonReportTierView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toCarbonReportTierViews(mutableListOf<CarbonReportTier>(item, item)).size, "views")
    }
}
