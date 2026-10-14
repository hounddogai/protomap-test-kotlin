package com.acme.transport.safety.scorecard.slot

import kotlin.test.Test
import kotlin.test.assertEquals

class ScorecardAssignmentViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newScorecardAssignment("code-372", "label-711")
        val view = toScorecardAssignmentView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toScorecardAssignmentViews(mutableListOf<ScorecardAssignment>(item, item)).size, "views")
    }
}
