package com.acme.transport.emissions.carbonreport.reportmapping

import kotlin.test.Test
import kotlin.test.assertEquals

class GlobalCarbonReportAdjustmentViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newGlobalCarbonReportAdjustment("code-211", "name-615")
        val view = toGlobalCarbonReportAdjustmentView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toGlobalCarbonReportAdjustmentViews(mutableListOf<GlobalCarbonReportAdjustment>(item, item)).size, "views")
    }
}
