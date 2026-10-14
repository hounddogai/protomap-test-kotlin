package com.acme.transport.fleetops.defect.manifest

import kotlin.test.Test
import kotlin.test.assertEquals

class DefectMilestoneViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newDefectMilestone("code-153", "label-501")
        val view = toDefectMilestoneView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toDefectMilestoneViews(mutableListOf<DefectMilestone>(item, item)).size, "views")
    }
}
