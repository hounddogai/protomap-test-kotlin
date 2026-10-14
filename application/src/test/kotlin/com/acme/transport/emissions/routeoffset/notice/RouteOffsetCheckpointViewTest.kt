package com.acme.transport.emissions.routeoffset.notice

import kotlin.test.Test
import kotlin.test.assertEquals

class RouteOffsetCheckpointViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newRouteOffsetCheckpoint("code-382", "name-390")
        val view = toRouteOffsetCheckpointView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toRouteOffsetCheckpointViews(mutableListOf<RouteOffsetCheckpoint>(item, item)).size, "views")
    }
}
