package com.acme.transport.emissions.carbonreport.budget

import kotlin.test.Test
import kotlin.test.assertEquals

class RegionalCarbonReportRequestViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newRegionalCarbonReportRequest("code-926", "title-468")
        val view = toRegionalCarbonReportRequestView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toRegionalCarbonReportRequestViews(mutableListOf<RegionalCarbonReportRequest>(item, item)).size, "views")
    }
}
