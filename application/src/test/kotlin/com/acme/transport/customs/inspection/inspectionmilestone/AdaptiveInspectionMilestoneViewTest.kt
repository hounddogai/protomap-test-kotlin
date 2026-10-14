package com.acme.transport.customs.inspection.inspectionmilestone

import kotlin.test.Test
import kotlin.test.assertEquals

class AdaptiveInspectionMilestoneViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newAdaptiveInspectionMilestone("code-436", "label-828")
        val view = toAdaptiveInspectionMilestoneView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toAdaptiveInspectionMilestoneViews(mutableListOf<AdaptiveInspectionMilestone>(item, item)).size, "views")
    }
}
