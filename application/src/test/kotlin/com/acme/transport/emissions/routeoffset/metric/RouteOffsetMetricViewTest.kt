package com.acme.transport.emissions.routeoffset.metric

import kotlin.test.Test
import kotlin.test.assertEquals

class RouteOffsetMetricViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newRouteOffsetMetric("code-69", "name-522")
        val view = toRouteOffsetMetricView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toRouteOffsetMetricViews(mutableListOf<RouteOffsetMetric>(item, item)).size, "views")
    }
}
