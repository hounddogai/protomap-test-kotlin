package com.acme.transport.emissions.carbonreport.forecast

import kotlin.test.Test
import kotlin.test.assertEquals

class ManualCarbonReportForecastViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newManualCarbonReportForecast("code-349", "label-70")
        val view = toManualCarbonReportForecastView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toManualCarbonReportForecastViews(mutableListOf<ManualCarbonReportForecast>(item, item)).size, "views")
    }
}
