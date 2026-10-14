package com.acme.transport.safety.nearmiss.schedule

import kotlin.test.Test
import kotlin.test.assertEquals

class ScheduledNearMissScheduleViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newScheduledNearMissSchedule("code-320", "label-579")
        val view = toScheduledNearMissScheduleView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toScheduledNearMissScheduleViews(mutableListOf<ScheduledNearMissSchedule>(item, item)).size, "views")
    }
}
