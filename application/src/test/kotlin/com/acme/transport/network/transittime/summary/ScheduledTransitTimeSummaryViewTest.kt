package com.acme.transport.network.transittime.summary

import kotlin.test.Test
import kotlin.test.assertEquals

class ScheduledTransitTimeSummaryViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newScheduledTransitTimeSummary("code-453", "name-292")
        val view = toScheduledTransitTimeSummaryView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toScheduledTransitTimeSummaryViews(mutableListOf<ScheduledTransitTimeSummary>(item, item)).size, "views")
    }
}
