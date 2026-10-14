package com.acme.transport.emissions.routeoffset.assignment

import kotlin.test.Test
import kotlin.test.assertEquals

class RouteOffsetTemplateViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newRouteOffsetTemplate("code-620", "name-672")
        val view = toRouteOffsetTemplateView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toRouteOffsetTemplateViews(mutableListOf<RouteOffsetTemplate>(item, item)).size, "views")
    }
}
