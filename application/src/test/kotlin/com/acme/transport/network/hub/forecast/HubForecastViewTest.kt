package com.acme.transport.network.hub.forecast

import kotlin.test.Test
import kotlin.test.assertEquals

class HubForecastViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newHubForecast("code-547", "name-960")
        val view = toHubForecastView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toHubForecastViews(mutableListOf<HubForecast>(item, item)).size, "views")
    }
}
