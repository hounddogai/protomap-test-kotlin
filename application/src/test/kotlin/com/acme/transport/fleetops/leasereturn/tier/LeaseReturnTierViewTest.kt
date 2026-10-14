package com.acme.transport.fleetops.leasereturn.tier

import kotlin.test.Test
import kotlin.test.assertEquals

class LeaseReturnTierViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newLeaseReturnTier("code-531", "label-133")
        val view = toLeaseReturnTierView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toLeaseReturnTierViews(mutableListOf<LeaseReturnTier>(item, item)).size, "views")
    }
}
