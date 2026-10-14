package com.acme.transport.safety.scorecard.milestone

import kotlin.test.Test
import kotlin.test.assertEquals

class ScorecardPlanViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newScorecardPlan("code-892", "label-342")
        val view = toScorecardPlanView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toScorecardPlanViews(mutableListOf<ScorecardPlan>(item, item)).size, "views")
    }
}
