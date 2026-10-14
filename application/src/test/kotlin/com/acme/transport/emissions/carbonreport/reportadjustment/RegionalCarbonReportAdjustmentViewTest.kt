package com.acme.transport.emissions.carbonreport.reportadjustment

import kotlin.test.Test
import kotlin.test.assertEquals

class RegionalCarbonReportAdjustmentViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newRegionalCarbonReportAdjustment("code-529", "name-122")
        val view = toRegionalCarbonReportAdjustmentView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toRegionalCarbonReportAdjustmentViews(mutableListOf<RegionalCarbonReportAdjustment>(item, item)).size, "views")
    }
}
