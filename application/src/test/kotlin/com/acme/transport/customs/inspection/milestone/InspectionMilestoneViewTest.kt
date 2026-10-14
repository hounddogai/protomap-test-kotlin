package com.acme.transport.customs.inspection.milestone

import kotlin.test.Test
import kotlin.test.assertEquals

class InspectionMilestoneViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newInspectionMilestone("code-501", "label-116")
        val view = toInspectionMilestoneView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toInspectionMilestoneViews(mutableListOf<InspectionMilestone>(item, item)).size, "views")
    }
}
