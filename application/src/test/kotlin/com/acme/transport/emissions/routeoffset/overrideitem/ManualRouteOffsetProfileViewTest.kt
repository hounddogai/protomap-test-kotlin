package com.acme.transport.emissions.routeoffset.overrideitem

import kotlin.test.Test
import kotlin.test.assertEquals

class ManualRouteOffsetProfileViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newManualRouteOffsetProfile("code-447", "name-464")
        val view = toManualRouteOffsetProfileView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toManualRouteOffsetProfileViews(mutableListOf<ManualRouteOffsetProfile>(item, item)).size, "views")
    }
}
