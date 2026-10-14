package com.acme.transport.customs.hold.forecast

import kotlin.test.Test
import kotlin.test.assertEquals

class ScheduledHoldForecastViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newScheduledHoldForecast("code-264", "label-328")
        val view = toScheduledHoldForecastView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toScheduledHoldForecastViews(mutableListOf<ScheduledHoldForecast>(item, item)).size, "views")
    }
}
