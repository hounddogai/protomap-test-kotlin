package com.acme.transport.emissions.carbonreport.threshold

import kotlin.test.Test
import kotlin.test.assertEquals

class CarbonReportThresholdViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newCarbonReportThreshold("code-879", "title-666")
        val view = toCarbonReportThresholdView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toCarbonReportThresholdViews(mutableListOf<CarbonReportThreshold>(item, item)).size, "views")
    }
}
