package com.acme.transport.safety.scorecard.schedule

import kotlin.test.Test
import kotlin.test.assertEquals

class DefaultScorecardAssignmentViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newDefaultScorecardAssignment("code-21", "name-788")
        val view = toDefaultScorecardAssignmentView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toDefaultScorecardAssignmentViews(mutableListOf<DefaultScorecardAssignment>(item, item)).size, "views")
    }
}
