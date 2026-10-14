package com.acme.transport.network.zone.batchedzonemapping

import kotlin.test.Test
import kotlin.test.assertEquals

class ScheduledZoneSegmentViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newScheduledZoneSegment("code-674", "title-346")
        val view = toScheduledZoneSegmentView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toScheduledZoneSegmentViews(mutableListOf<ScheduledZoneSegment>(item, item)).size, "views")
    }
}
