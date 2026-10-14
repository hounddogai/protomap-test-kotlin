package com.acme.transport.emissions.carbonreport.score

import kotlin.test.Test
import kotlin.test.assertEquals

class RegionalCarbonReportScoreViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newRegionalCarbonReportScore("code-700", "title-115")
        val view = toRegionalCarbonReportScoreView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toRegionalCarbonReportScoreViews(mutableListOf<RegionalCarbonReportScore>(item, item)).size, "views")
    }
}
