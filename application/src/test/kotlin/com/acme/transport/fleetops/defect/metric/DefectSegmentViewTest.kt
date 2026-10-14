package com.acme.transport.fleetops.defect.metric

import kotlin.test.Test
import kotlin.test.assertEquals

class DefectSegmentViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newDefectSegment("code-243", "label-348")
        val view = toDefectSegmentView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toDefectSegmentViews(mutableListOf<DefectSegment>(item, item)).size, "views")
    }
}
