package com.acme.transport.safety.scorecard.milestone

import kotlin.test.Test
import kotlin.test.assertEquals

class ScorecardMilestoneViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newScorecardMilestone("code-903", "title-118")
        val view = toScorecardMilestoneView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toScorecardMilestoneViews(mutableListOf<ScorecardMilestone>(item, item)).size, "views")
    }
}
