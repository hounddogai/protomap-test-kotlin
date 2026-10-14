package com.acme.transport.customs.inspection.inspectionrule

import kotlin.test.Test
import kotlin.test.assertEquals

class FallbackInspectionSegmentViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newFallbackInspectionSegment("code-249", "title-964")
        val view = toFallbackInspectionSegmentView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toFallbackInspectionSegmentViews(mutableListOf<FallbackInspectionSegment>(item, item)).size, "views")
    }
}
