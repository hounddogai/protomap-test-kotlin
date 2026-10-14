package com.acme.transport.emissions.carbonreport.rule

import kotlin.test.Test
import kotlin.test.assertEquals

class ScheduledCarbonReportCycleViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newScheduledCarbonReportCycle("code-146", "title-183")
        val view = toScheduledCarbonReportCycleView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toScheduledCarbonReportCycleViews(mutableListOf<ScheduledCarbonReportCycle>(item, item)).size, "views")
    }
}
