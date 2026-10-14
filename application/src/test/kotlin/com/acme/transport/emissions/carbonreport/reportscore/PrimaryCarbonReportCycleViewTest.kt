package com.acme.transport.emissions.carbonreport.reportscore

import kotlin.test.Test
import kotlin.test.assertEquals

class PrimaryCarbonReportCycleViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newPrimaryCarbonReportCycle("code-109", "name-87")
        val view = toPrimaryCarbonReportCycleView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toPrimaryCarbonReportCycleViews(mutableListOf<PrimaryCarbonReportCycle>(item, item)).size, "views")
    }
}
