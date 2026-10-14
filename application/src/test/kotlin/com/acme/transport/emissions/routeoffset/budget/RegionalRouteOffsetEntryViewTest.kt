package com.acme.transport.emissions.routeoffset.budget

import kotlin.test.Test
import kotlin.test.assertEquals

class RegionalRouteOffsetEntryViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newRegionalRouteOffsetEntry("code-837", "name-470")
        val view = toRegionalRouteOffsetEntryView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toRegionalRouteOffsetEntryViews(mutableListOf<RegionalRouteOffsetEntry>(item, item)).size, "views")
    }
}
