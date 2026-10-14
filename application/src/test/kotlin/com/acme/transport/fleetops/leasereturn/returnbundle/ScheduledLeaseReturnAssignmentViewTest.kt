package com.acme.transport.fleetops.leasereturn.returnbundle

import kotlin.test.Test
import kotlin.test.assertEquals

class ScheduledLeaseReturnAssignmentViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newScheduledLeaseReturnAssignment("code-865", "name-436")
        val view = toScheduledLeaseReturnAssignmentView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toScheduledLeaseReturnAssignmentViews(mutableListOf<ScheduledLeaseReturnAssignment>(item, item)).size, "views")
    }
}
