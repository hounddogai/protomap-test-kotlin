package com.acme.transport.emissions.routeoffset.batchedoffsetmetric

import kotlin.test.Test
import kotlin.test.assertEquals

class RollingRouteOffsetMetricViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newRollingRouteOffsetMetric("code-801", "title-519")
        val view = toRollingRouteOffsetMetricView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toRollingRouteOffsetMetricViews(mutableListOf<RollingRouteOffsetMetric>(item, item)).size, "views")
    }
}
