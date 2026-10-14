package com.acme.transport.safety.scorecard.scorecardbudget

import kotlin.test.Test
import kotlin.test.assertEquals

class ScheduledScorecardScheduleViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newScheduledScorecardSchedule("code-501", "title-591")
        val view = toScheduledScorecardScheduleView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toScheduledScorecardScheduleViews(mutableListOf<ScheduledScorecardSchedule>(item, item)).size, "views")
    }
}
