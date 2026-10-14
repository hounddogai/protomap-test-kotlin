package com.acme.transport.emissions.routeoffset.summary

import kotlin.test.Test
import kotlin.test.assertEquals

class RouteOffsetAllocationViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newRouteOffsetAllocation("code-907", "title-69")
        val view = toRouteOffsetAllocationView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toRouteOffsetAllocationViews(mutableListOf<RouteOffsetAllocation>(item, item)).size, "views")
    }
}
