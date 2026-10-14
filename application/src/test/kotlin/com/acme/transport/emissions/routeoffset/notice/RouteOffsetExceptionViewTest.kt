package com.acme.transport.emissions.routeoffset.notice

import kotlin.test.Test
import kotlin.test.assertEquals

class RouteOffsetExceptionViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newRouteOffsetException("code-614", "title-402")
        val view = toRouteOffsetExceptionView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toRouteOffsetExceptionViews(mutableListOf<RouteOffsetException>(item, item)).size, "views")
    }
}
