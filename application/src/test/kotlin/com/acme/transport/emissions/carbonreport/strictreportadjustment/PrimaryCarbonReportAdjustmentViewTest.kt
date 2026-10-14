package com.acme.transport.emissions.carbonreport.strictreportadjustment

import kotlin.test.Test
import kotlin.test.assertEquals

class PrimaryCarbonReportAdjustmentViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newPrimaryCarbonReportAdjustment("code-725", "title-532")
        val view = toPrimaryCarbonReportAdjustmentView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toPrimaryCarbonReportAdjustmentViews(mutableListOf<PrimaryCarbonReportAdjustment>(item, item)).size, "views")
    }
}
