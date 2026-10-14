package com.acme.transport.customs.inspection.entry

import kotlin.test.Test
import kotlin.test.assertEquals

class LegacyInspectionScheduleViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newLegacyInspectionSchedule("code-587", "label-291")
        val view = toLegacyInspectionScheduleView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toLegacyInspectionScheduleViews(mutableListOf<LegacyInspectionSchedule>(item, item)).size, "views")
    }
}
