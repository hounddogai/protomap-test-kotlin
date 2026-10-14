package com.acme.transport.emissions.idling.summary

import kotlin.test.Test
import kotlin.test.assertEquals

class RegionalIdlingSummaryViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newRegionalIdlingSummary("code-700", "name-754")
        val view = toRegionalIdlingSummaryView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toRegionalIdlingSummaryViews(mutableListOf<RegionalIdlingSummary>(item, item)).size, "views")
    }
}
