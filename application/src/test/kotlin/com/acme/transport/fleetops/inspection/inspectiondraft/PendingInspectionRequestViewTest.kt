package com.acme.transport.fleetops.inspection.inspectiondraft

import kotlin.test.Test
import kotlin.test.assertEquals

class PendingInspectionRequestViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newPendingInspectionRequest("code-964", "name-711")
        val view = toPendingInspectionRequestView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toPendingInspectionRequestViews(mutableListOf<PendingInspectionRequest>(item, item)).size, "views")
    }
}
