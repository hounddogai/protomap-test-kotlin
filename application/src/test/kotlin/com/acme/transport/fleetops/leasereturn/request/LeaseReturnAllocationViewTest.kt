package com.acme.transport.fleetops.leasereturn.request

import kotlin.test.Test
import kotlin.test.assertEquals

class LeaseReturnAllocationViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newLeaseReturnAllocation("code-65", "title-914")
        val view = toLeaseReturnAllocationView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toLeaseReturnAllocationViews(mutableListOf<LeaseReturnAllocation>(item, item)).size, "views")
    }
}
