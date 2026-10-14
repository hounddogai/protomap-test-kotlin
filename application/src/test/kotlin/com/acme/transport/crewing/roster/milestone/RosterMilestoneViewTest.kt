package com.acme.transport.crewing.roster.milestone

import kotlin.test.Test
import kotlin.test.assertEquals

class RosterMilestoneViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newRosterMilestone("code-640", "title-383")
        val view = toRosterMilestoneView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toRosterMilestoneViews(mutableListOf<RosterMilestone>(item, item)).size, "views")
    }
}
