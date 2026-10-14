package com.acme.transport.fleetops.leasereturn.budget

import kotlin.test.Test
import kotlin.test.assertEquals

class GlobalLeaseReturnAssignmentViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newGlobalLeaseReturnAssignment("code-142", "title-578")
        val view = toGlobalLeaseReturnAssignmentView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toGlobalLeaseReturnAssignmentViews(mutableListOf<GlobalLeaseReturnAssignment>(item, item)).size, "views")
    }
}
