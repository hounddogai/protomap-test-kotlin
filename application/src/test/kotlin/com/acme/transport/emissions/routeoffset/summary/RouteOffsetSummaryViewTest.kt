package com.acme.transport.emissions.routeoffset.summary

import kotlin.test.Test
import kotlin.test.assertEquals

class RouteOffsetSummaryViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newRouteOffsetSummary("code-206", "title-33")
        val view = toRouteOffsetSummaryView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toRouteOffsetSummaryViews(mutableListOf<RouteOffsetSummary>(item, item)).size, "views")
    }
}
