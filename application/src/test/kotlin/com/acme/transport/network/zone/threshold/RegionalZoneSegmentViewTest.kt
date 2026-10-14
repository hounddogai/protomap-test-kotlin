package com.acme.transport.network.zone.threshold

import kotlin.test.Test
import kotlin.test.assertEquals

class RegionalZoneSegmentViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newRegionalZoneSegment("code-470", "title-57")
        val view = toRegionalZoneSegmentView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toRegionalZoneSegmentViews(mutableListOf<RegionalZoneSegment>(item, item)).size, "views")
    }
}
