package com.acme.transport.emissions.routeoffset.metric

import kotlin.test.Test
import kotlin.test.assertEquals

class RouteOffsetClaimViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newRouteOffsetClaim("code-260", "label-363")
        val view = toRouteOffsetClaimView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toRouteOffsetClaimViews(mutableListOf<RouteOffsetClaim>(item, item)).size, "views")
    }
}
