package com.acme.transport.crewing.roster.summary

import kotlin.test.Test
import kotlin.test.assertEquals

class RosterSummaryViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newRosterSummary("code-464", "label-279")
        val view = toRosterSummaryView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toRosterSummaryViews(mutableListOf<RosterSummary>(item, item)).size, "views")
    }
}
