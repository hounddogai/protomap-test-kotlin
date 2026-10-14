package com.acme.transport.lastmile.parcel.quota

import kotlin.test.Test
import kotlin.test.assertEquals

class ScheduledParcelSummaryViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newScheduledParcelSummary("code-313", "label-520")
        val view = toScheduledParcelSummaryView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toScheduledParcelSummaryViews(mutableListOf<ScheduledParcelSummary>(item, item)).size, "views")
    }
}
