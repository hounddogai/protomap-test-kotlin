package com.acme.transport.fleetops.inspection.reservation

import kotlin.test.Test
import kotlin.test.assertEquals

class LenientInspectionSegmentViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newLenientInspectionSegment("code-499", "label-145")
        val view = toLenientInspectionSegmentView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toLenientInspectionSegmentViews(mutableListOf<LenientInspectionSegment>(item, item)).size, "views")
    }
}
