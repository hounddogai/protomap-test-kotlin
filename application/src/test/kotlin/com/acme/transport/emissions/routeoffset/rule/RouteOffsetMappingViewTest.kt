package com.acme.transport.emissions.routeoffset.rule

import kotlin.test.Test
import kotlin.test.assertEquals

class RouteOffsetMappingViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newRouteOffsetMapping("code-177", "title-997")
        val view = toRouteOffsetMappingView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toRouteOffsetMappingViews(mutableListOf<RouteOffsetMapping>(item, item)).size, "views")
    }
}
