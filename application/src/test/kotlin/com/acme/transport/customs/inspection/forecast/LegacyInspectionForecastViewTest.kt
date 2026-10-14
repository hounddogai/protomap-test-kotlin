package com.acme.transport.customs.inspection.forecast

import kotlin.test.Test
import kotlin.test.assertEquals

class LegacyInspectionForecastViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newLegacyInspectionForecast("code-977", "label-864")
        val view = toLegacyInspectionForecastView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toLegacyInspectionForecastViews(mutableListOf<LegacyInspectionForecast>(item, item)).size, "views")
    }
}
