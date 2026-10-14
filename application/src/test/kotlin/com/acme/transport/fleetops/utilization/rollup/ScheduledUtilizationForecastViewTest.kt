package com.acme.transport.fleetops.utilization.rollup

import kotlin.test.Test
import kotlin.test.assertEquals

class ScheduledUtilizationForecastViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newScheduledUtilizationForecast("code-135", "name-671")
        val view = toScheduledUtilizationForecastView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toScheduledUtilizationForecastViews(mutableListOf<ScheduledUtilizationForecast>(item, item)).size, "views")
    }
}
