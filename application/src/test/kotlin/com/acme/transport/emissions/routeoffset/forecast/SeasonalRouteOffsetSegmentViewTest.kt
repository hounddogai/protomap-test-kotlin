package com.acme.transport.emissions.routeoffset.forecast

import kotlin.test.Test
import kotlin.test.assertEquals

class SeasonalRouteOffsetSegmentViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newSeasonalRouteOffsetSegment("code-157", "title-775")
        val view = toSeasonalRouteOffsetSegmentView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toSeasonalRouteOffsetSegmentViews(mutableListOf<SeasonalRouteOffsetSegment>(item, item)).size, "views")
    }
}
