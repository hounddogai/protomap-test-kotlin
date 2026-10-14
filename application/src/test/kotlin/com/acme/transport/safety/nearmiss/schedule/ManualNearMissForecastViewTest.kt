package com.acme.transport.safety.nearmiss.schedule

import kotlin.test.Test
import kotlin.test.assertEquals

class ManualNearMissForecastViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newManualNearMissForecast("code-933", "label-443")
        val view = toManualNearMissForecastView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toManualNearMissForecastViews(mutableListOf<ManualNearMissForecast>(item, item)).size, "views")
    }
}
