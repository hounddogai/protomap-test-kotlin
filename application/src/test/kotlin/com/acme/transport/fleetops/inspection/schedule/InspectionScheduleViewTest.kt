package com.acme.transport.fleetops.inspection.schedule

import kotlin.test.Test
import kotlin.test.assertEquals

class InspectionScheduleViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newInspectionSchedule("code-479", "title-771")
        val view = toInspectionScheduleView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toInspectionScheduleViews(mutableListOf<InspectionSchedule>(item, item)).size, "views")
    }
}
