package com.acme.transport.fleetops.leasereturn.returnmilestone

import kotlin.test.Test
import kotlin.test.assertEquals

class PendingLeaseReturnAssignmentViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newPendingLeaseReturnAssignment("code-734", "title-219")
        val view = toPendingLeaseReturnAssignmentView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toPendingLeaseReturnAssignmentViews(mutableListOf<PendingLeaseReturnAssignment>(item, item)).size, "views")
    }
}
