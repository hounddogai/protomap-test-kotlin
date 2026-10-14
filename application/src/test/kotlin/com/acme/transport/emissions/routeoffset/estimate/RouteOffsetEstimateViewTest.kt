package com.acme.transport.emissions.routeoffset.estimate

import kotlin.test.Test
import kotlin.test.assertEquals

class RouteOffsetEstimateViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newRouteOffsetEstimate("code-851", "name-723")
        val view = toRouteOffsetEstimateView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toRouteOffsetEstimateViews(mutableListOf<RouteOffsetEstimate>(item, item)).size, "views")
    }
}
