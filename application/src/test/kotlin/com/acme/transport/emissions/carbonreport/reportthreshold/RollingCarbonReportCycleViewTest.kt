package com.acme.transport.emissions.carbonreport.reportthreshold

import kotlin.test.Test
import kotlin.test.assertEquals

class RollingCarbonReportCycleViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newRollingCarbonReportCycle("code-897", "name-894")
        val view = toRollingCarbonReportCycleView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toRollingCarbonReportCycleViews(mutableListOf<RollingCarbonReportCycle>(item, item)).size, "views")
    }
}
