package com.acme.transport.fleetops.inspection.overrideitem

import kotlin.test.Test
import kotlin.test.assertEquals

class ManualInspectionForecastViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newManualInspectionForecast("code-852", "title-94")
        val view = toManualInspectionForecastView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toManualInspectionForecastViews(mutableListOf<ManualInspectionForecast>(item, item)).size, "views")
    }
}
