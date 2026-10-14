package com.acme.transport.emissions.routeoffset.notice

import kotlin.test.Test
import kotlin.test.assertEquals

class RouteOffsetNoticeViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newRouteOffsetNotice("code-500", "label-475")
        val view = toRouteOffsetNoticeView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toRouteOffsetNoticeViews(mutableListOf<RouteOffsetNotice>(item, item)).size, "views")
    }
}
