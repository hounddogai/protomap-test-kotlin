package com.acme.transport.fleetops.serviceplan.summary

import kotlin.test.Test
import kotlin.test.assertEquals

class ServicePlanForecastViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newServicePlanForecast("code-744", "label-44")
        val view = toServicePlanForecastView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toServicePlanForecastViews(mutableListOf<ServicePlanForecast>(item, item)).size, "views")
    }
}
