package com.acme.transport.emissions.routeoffset.segment

import kotlin.test.Test
import kotlin.test.assertEquals

class RouteOffsetScoreViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newRouteOffsetScore("code-112", "label-103")
        val view = toRouteOffsetScoreView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toRouteOffsetScoreViews(mutableListOf<RouteOffsetScore>(item, item)).size, "views")
    }
}
