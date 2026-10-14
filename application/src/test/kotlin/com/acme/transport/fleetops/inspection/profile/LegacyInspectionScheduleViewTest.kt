package com.acme.transport.fleetops.inspection.profile

import kotlin.test.Test
import kotlin.test.assertEquals

class LegacyInspectionScheduleViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newLegacyInspectionSchedule("code-868", "name-867")
        val view = toLegacyInspectionScheduleView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toLegacyInspectionScheduleViews(mutableListOf<LegacyInspectionSchedule>(item, item)).size, "views")
    }
}
