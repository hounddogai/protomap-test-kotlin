package com.acme.transport.fleetops.leasereturn.proposal

import kotlin.test.Test
import kotlin.test.assertEquals

class GlobalLeaseReturnAdjustmentViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newGlobalLeaseReturnAdjustment("code-474", "name-970")
        val view = toGlobalLeaseReturnAdjustmentView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toGlobalLeaseReturnAdjustmentViews(mutableListOf<GlobalLeaseReturnAdjustment>(item, item)).size, "views")
    }
}
