package com.acme.transport.fleetops.defect.defectprofile

import kotlin.test.Test
import kotlin.test.assertEquals

class RegionalDefectScheduleViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newRegionalDefectSchedule("code-746", "label-299")
        val view = toRegionalDefectScheduleView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toRegionalDefectScheduleViews(mutableListOf<RegionalDefectSchedule>(item, item)).size, "views")
    }
}
