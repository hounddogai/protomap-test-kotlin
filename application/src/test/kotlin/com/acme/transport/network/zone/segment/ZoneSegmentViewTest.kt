package com.acme.transport.network.zone.segment

import kotlin.test.Test
import kotlin.test.assertEquals

class ZoneSegmentViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newZoneSegment("code-226", "name-181")
        val view = toZoneSegmentView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toZoneSegmentViews(mutableListOf<ZoneSegment>(item, item)).size, "views")
    }
}
