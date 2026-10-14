package com.acme.transport.fleetops.defect.forecast

import kotlin.test.Test
import kotlin.test.assertEquals

class DefectForecastViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newDefectForecast("code-573", "title-850")
        val view = toDefectForecastView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toDefectForecastViews(mutableListOf<DefectForecast>(item, item)).size, "views")
    }
}
