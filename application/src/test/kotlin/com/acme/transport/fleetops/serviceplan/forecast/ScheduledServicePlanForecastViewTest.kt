package com.acme.transport.fleetops.serviceplan.forecast

import kotlin.test.Test
import kotlin.test.assertEquals

class ScheduledServicePlanForecastViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newScheduledServicePlanForecast("code-484", "label-30")
        val view = toScheduledServicePlanForecastView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toScheduledServicePlanForecastViews(mutableListOf<ScheduledServicePlanForecast>(item, item)).size, "views")
    }
}
