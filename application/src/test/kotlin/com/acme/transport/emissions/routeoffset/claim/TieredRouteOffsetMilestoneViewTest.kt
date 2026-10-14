package com.acme.transport.emissions.routeoffset.claim

import kotlin.test.Test
import kotlin.test.assertEquals

class TieredRouteOffsetMilestoneViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newTieredRouteOffsetMilestone("code-583", "title-650")
        val view = toTieredRouteOffsetMilestoneView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toTieredRouteOffsetMilestoneViews(mutableListOf<TieredRouteOffsetMilestone>(item, item)).size, "views")
    }
}
