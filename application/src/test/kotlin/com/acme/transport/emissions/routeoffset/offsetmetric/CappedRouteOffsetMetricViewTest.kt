package com.acme.transport.emissions.routeoffset.offsetmetric

import kotlin.test.Test
import kotlin.test.assertEquals

class CappedRouteOffsetMetricViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newCappedRouteOffsetMetric("code-336", "name-553")
        val view = toCappedRouteOffsetMetricView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toCappedRouteOffsetMetricViews(mutableListOf<CappedRouteOffsetMetric>(item, item)).size, "views")
    }
}
