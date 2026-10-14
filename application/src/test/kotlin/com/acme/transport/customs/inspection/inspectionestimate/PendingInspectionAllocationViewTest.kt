package com.acme.transport.customs.inspection.inspectionestimate

import kotlin.test.Test
import kotlin.test.assertEquals

class PendingInspectionAllocationViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newPendingInspectionAllocation("code-697", "title-87")
        val view = toPendingInspectionAllocationView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toPendingInspectionAllocationViews(mutableListOf<PendingInspectionAllocation>(item, item)).size, "views")
    }
}
