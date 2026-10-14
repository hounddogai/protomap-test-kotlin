package com.acme.transport.fleetops.inspection.inspectiondraft

import kotlin.test.Test
import kotlin.test.assertEquals

class DefaultInspectionScheduleViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newDefaultInspectionSchedule("code-259", "name-34")
        val view = toDefaultInspectionScheduleView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toDefaultInspectionScheduleViews(mutableListOf<DefaultInspectionSchedule>(item, item)).size, "views")
    }
}
