package com.acme.transport.emissions.carbonreport.reportmapping

import kotlin.test.Test
import kotlin.test.assertEquals

class StrictCarbonReportMappingViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newStrictCarbonReportMapping("code-53", "label-684")
        val view = toStrictCarbonReportMappingView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toStrictCarbonReportMappingViews(mutableListOf<StrictCarbonReportMapping>(item, item)).size, "views")
    }
}
