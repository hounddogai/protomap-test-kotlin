package com.acme.transport.fleetops.leasereturn.returnentry

import kotlin.test.Test
import kotlin.test.assertEquals

class ScheduledLeaseReturnEstimateViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newScheduledLeaseReturnEstimate("code-556", "label-727")
        val view = toScheduledLeaseReturnEstimateView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toScheduledLeaseReturnEstimateViews(mutableListOf<ScheduledLeaseReturnEstimate>(item, item)).size, "views")
    }
}
