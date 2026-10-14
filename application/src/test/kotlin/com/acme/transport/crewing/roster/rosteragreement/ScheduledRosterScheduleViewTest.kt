package com.acme.transport.crewing.roster.rosteragreement

import kotlin.test.Test
import kotlin.test.assertEquals

class ScheduledRosterScheduleViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newScheduledRosterSchedule("code-148", "label-811")
        val view = toScheduledRosterScheduleView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toScheduledRosterScheduleViews(mutableListOf<ScheduledRosterSchedule>(item, item)).size, "views")
    }
}
