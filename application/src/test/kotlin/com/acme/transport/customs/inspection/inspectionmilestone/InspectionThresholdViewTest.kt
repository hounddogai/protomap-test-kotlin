package com.acme.transport.customs.inspection.inspectionmilestone

import kotlin.test.Test
import kotlin.test.assertEquals

class InspectionThresholdViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newInspectionThreshold("code-959", "name-980")
        val view = toInspectionThresholdView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toInspectionThresholdViews(mutableListOf<InspectionThreshold>(item, item)).size, "views")
    }
}
