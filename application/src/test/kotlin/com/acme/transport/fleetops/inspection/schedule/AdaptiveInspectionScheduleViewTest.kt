package com.acme.transport.fleetops.inspection.schedule

import kotlin.test.Test
import kotlin.test.assertEquals

class AdaptiveInspectionScheduleViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newAdaptiveInspectionSchedule("code-837", "title-993")
        val view = toAdaptiveInspectionScheduleView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toAdaptiveInspectionScheduleViews(mutableListOf<AdaptiveInspectionSchedule>(item, item)).size, "views")
    }
}
