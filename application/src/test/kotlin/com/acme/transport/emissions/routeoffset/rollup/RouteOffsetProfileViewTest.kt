package com.acme.transport.emissions.routeoffset.rollup

import kotlin.test.Test
import kotlin.test.assertEquals

class RouteOffsetProfileViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newRouteOffsetProfile("code-145", "label-267")
        val view = toRouteOffsetProfileView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toRouteOffsetProfileViews(mutableListOf<RouteOffsetProfile>(item, item)).size, "views")
    }
}
