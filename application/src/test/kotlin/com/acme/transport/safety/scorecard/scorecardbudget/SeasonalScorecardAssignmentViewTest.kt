package com.acme.transport.safety.scorecard.scorecardbudget

import kotlin.test.Test
import kotlin.test.assertEquals

class SeasonalScorecardAssignmentViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newSeasonalScorecardAssignment("code-468", "label-6")
        val view = toSeasonalScorecardAssignmentView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toSeasonalScorecardAssignmentViews(mutableListOf<SeasonalScorecardAssignment>(item, item)).size, "views")
    }
}
