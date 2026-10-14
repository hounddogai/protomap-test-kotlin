package com.acme.transport.customs.inspection.metric

import kotlin.test.Test
import kotlin.test.assertEquals

class InspectionSegmentViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newInspectionSegment("code-442", "label-410")
        val view = toInspectionSegmentView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toInspectionSegmentViews(mutableListOf<InspectionSegment>(item, item)).size, "views")
    }
}
