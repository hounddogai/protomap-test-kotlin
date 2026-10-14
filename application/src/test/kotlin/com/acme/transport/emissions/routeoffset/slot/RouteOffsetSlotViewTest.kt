package com.acme.transport.emissions.routeoffset.slot

import kotlin.test.Test
import kotlin.test.assertEquals

class RouteOffsetSlotViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newRouteOffsetSlot("code-358", "title-168")
        val view = toRouteOffsetSlotView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toRouteOffsetSlotViews(mutableListOf<RouteOffsetSlot>(item, item)).size, "views")
    }
}
