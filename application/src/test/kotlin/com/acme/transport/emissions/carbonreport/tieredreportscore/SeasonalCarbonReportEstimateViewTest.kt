package com.acme.transport.emissions.carbonreport.tieredreportscore

import kotlin.test.Test
import kotlin.test.assertEquals

class SeasonalCarbonReportEstimateViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newSeasonalCarbonReportEstimate("code-758", "name-820")
        val view = toSeasonalCarbonReportEstimateView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toSeasonalCarbonReportEstimateViews(mutableListOf<SeasonalCarbonReportEstimate>(item, item)).size, "views")
    }
}
