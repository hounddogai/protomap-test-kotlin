package com.acme.transport.fleetops.tire.summary

import kotlin.test.Test
import kotlin.test.assertEquals

class TireSummaryViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newTireSummary("code-874", "title-938")
        val view = toTireSummaryView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toTireSummaryViews(mutableListOf<TireSummary>(item, item)).size, "views")
    }
}
