package com.acme.transport.emissions.routeoffset.offsetcheckpoint

import kotlin.test.Test
import kotlin.test.assertEquals

class RegionalRouteOffsetSummaryViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newRegionalRouteOffsetSummary("code-180", "label-827")
        val view = toRegionalRouteOffsetSummaryView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toRegionalRouteOffsetSummaryViews(mutableListOf<RegionalRouteOffsetSummary>(item, item)).size, "views")
    }
}
