package com.acme.transport.emissions.routeoffset.offsetbaseline

import kotlin.test.Test
import kotlin.test.assertEquals

class CappedRouteOffsetBaselineViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newCappedRouteOffsetBaseline("code-183", "name-853")
        val view = toCappedRouteOffsetBaselineView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toCappedRouteOffsetBaselineViews(mutableListOf<CappedRouteOffsetBaseline>(item, item)).size, "views")
    }
}
