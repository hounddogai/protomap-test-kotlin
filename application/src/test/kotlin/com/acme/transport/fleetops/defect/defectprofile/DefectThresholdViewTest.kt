package com.acme.transport.fleetops.defect.defectprofile

import kotlin.test.Test
import kotlin.test.assertEquals

class DefectThresholdViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newDefectThreshold("code-299", "title-845")
        val view = toDefectThresholdView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toDefectThresholdViews(mutableListOf<DefectThreshold>(item, item)).size, "views")
    }
}
