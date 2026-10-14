package com.acme.transport.terminal.dwell.schedule

import kotlin.test.Test
import kotlin.test.assertEquals

class ScheduledDwellScheduleViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newScheduledDwellSchedule("code-176", "name-969")
        val view = toScheduledDwellScheduleView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toScheduledDwellScheduleViews(mutableListOf<ScheduledDwellSchedule>(item, item)).size, "views")
    }
}
