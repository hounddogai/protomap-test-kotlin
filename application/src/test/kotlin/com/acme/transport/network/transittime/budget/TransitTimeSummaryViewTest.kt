package com.acme.transport.network.transittime.budget

import kotlin.test.Test
import kotlin.test.assertEquals

class TransitTimeSummaryViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newTransitTimeSummary("code-465", "name-907")
        val view = toTransitTimeSummaryView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toTransitTimeSummaryViews(mutableListOf<TransitTimeSummary>(item, item)).size, "views")
    }
}
