package com.acme.transport.emissions.routeoffset.forecast

import kotlin.test.Test
import kotlin.test.assertEquals

class RouteOffsetForecastViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newRouteOffsetForecast("code-291", "name-274")
        val view = toRouteOffsetForecastView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toRouteOffsetForecastViews(mutableListOf<RouteOffsetForecast>(item, item)).size, "views")
    }
}
