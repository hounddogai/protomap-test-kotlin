package com.acme.transport.fleetops.inspection.inspectionschedule

import kotlin.test.Test
import kotlin.test.assertEquals

class BatchedInspectionSlotViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newBatchedInspectionSlot("code-927", "title-705")
        val view = toBatchedInspectionSlotView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toBatchedInspectionSlotViews(mutableListOf<BatchedInspectionSlot>(item, item)).size, "views")
    }
}
