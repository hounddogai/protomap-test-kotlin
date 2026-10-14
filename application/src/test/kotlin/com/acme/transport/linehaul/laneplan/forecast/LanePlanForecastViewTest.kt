package com.acme.transport.linehaul.laneplan.forecast

import kotlin.test.Test
import kotlin.test.assertEquals

class LanePlanForecastViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newLanePlanForecast("code-616", "title-51")
        val view = toLanePlanForecastView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toLanePlanForecastViews(mutableListOf<LanePlanForecast>(item, item)).size, "views")
    }
}
