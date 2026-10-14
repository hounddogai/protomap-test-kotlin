package com.acme.transport.fleetops.leasereturn.returnschedule

import kotlin.test.Test
import kotlin.test.assertEquals

class LeaseReturnRollupViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newLeaseReturnRollup("code-591", "name-222")
        val view = toLeaseReturnRollupView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toLeaseReturnRollupViews(mutableListOf<LeaseReturnRollup>(item, item)).size, "views")
    }
}
