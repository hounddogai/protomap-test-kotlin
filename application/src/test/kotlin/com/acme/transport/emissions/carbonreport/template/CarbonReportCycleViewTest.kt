package com.acme.transport.emissions.carbonreport.template

import kotlin.test.Test
import kotlin.test.assertEquals

class CarbonReportCycleViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newCarbonReportCycle("code-608", "label-985")
        val view = toCarbonReportCycleView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toCarbonReportCycleViews(mutableListOf<CarbonReportCycle>(item, item)).size, "views")
    }
}
