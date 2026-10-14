package com.acme.transport.emissions.carbonreport.reportthreshold

import kotlin.test.Test
import kotlin.test.assertEquals

class ScheduledCarbonReportWindowViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newScheduledCarbonReportWindow("code-360", "title-997")
        val view = toScheduledCarbonReportWindowView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toScheduledCarbonReportWindowViews(mutableListOf<ScheduledCarbonReportWindow>(item, item)).size, "views")
    }
}
