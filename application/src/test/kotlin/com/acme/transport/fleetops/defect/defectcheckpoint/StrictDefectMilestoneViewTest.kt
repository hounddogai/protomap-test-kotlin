package com.acme.transport.fleetops.defect.defectcheckpoint

import kotlin.test.Test
import kotlin.test.assertEquals

class StrictDefectMilestoneViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newStrictDefectMilestone("code-821", "label-62")
        val view = toStrictDefectMilestoneView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toStrictDefectMilestoneViews(mutableListOf<StrictDefectMilestone>(item, item)).size, "views")
    }
}
