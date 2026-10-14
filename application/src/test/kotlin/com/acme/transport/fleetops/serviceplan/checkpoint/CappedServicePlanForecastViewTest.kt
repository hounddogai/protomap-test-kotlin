package com.acme.transport.fleetops.serviceplan.checkpoint

import kotlin.test.Test
import kotlin.test.assertEquals

class CappedServicePlanForecastViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newCappedServicePlanForecast("code-531", "title-607")
        val view = toCappedServicePlanForecastView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toCappedServicePlanForecastViews(mutableListOf<CappedServicePlanForecast>(item, item)).size, "views")
    }
}
