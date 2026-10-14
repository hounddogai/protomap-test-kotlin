package com.acme.transport.fleetops.inspection.inspectionschedule

import kotlin.test.Test
import kotlin.test.assertEquals

class GlobalInspectionBatchViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newGlobalInspectionBatch("code-888", "label-62")
        val view = toGlobalInspectionBatchView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toGlobalInspectionBatchViews(mutableListOf<GlobalInspectionBatch>(item, item)).size, "views")
    }
}
