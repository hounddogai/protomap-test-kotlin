package com.acme.transport.emissions.carbonreport.rule

import kotlin.test.Test
import kotlin.test.assertEquals

class CarbonReportMilestoneViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newCarbonReportMilestone("code-994", "label-234")
        val view = toCarbonReportMilestoneView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toCarbonReportMilestoneViews(mutableListOf<CarbonReportMilestone>(item, item)).size, "views")
    }
}
