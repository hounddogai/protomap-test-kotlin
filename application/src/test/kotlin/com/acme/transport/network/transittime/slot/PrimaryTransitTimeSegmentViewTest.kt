package com.acme.transport.network.transittime.slot

import kotlin.test.Test
import kotlin.test.assertEquals

class PrimaryTransitTimeSegmentViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newPrimaryTransitTimeSegment("code-479", "label-437")
        val view = toPrimaryTransitTimeSegmentView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toPrimaryTransitTimeSegmentViews(mutableListOf<PrimaryTransitTimeSegment>(item, item)).size, "views")
    }
}
