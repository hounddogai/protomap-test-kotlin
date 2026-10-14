package com.acme.transport.fleetops.leasereturn.milestone

import kotlin.test.Test
import kotlin.test.assertEquals

class LeaseReturnMilestoneViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newLeaseReturnMilestone("code-426", "name-413")
        val view = toLeaseReturnMilestoneView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toLeaseReturnMilestoneViews(mutableListOf<LeaseReturnMilestone>(item, item)).size, "views")
    }
}
