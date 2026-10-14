package com.acme.transport.network.zone.reservation

import kotlin.test.Test
import kotlin.test.assertEquals

class BatchedZoneSegmentViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newBatchedZoneSegment("code-377", "name-511")
        val view = toBatchedZoneSegmentView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toBatchedZoneSegmentViews(mutableListOf<BatchedZoneSegment>(item, item)).size, "views")
    }
}
