package com.acme.transport.customs.clearance.quota

import kotlin.test.Test
import kotlin.test.assertEquals

class PendingClearanceAllocationViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newPendingClearanceAllocation("code-762", "title-684")
        val view = toPendingClearanceAllocationView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toPendingClearanceAllocationViews(mutableListOf<PendingClearanceAllocation>(item, item)).size, "views")
    }
}
