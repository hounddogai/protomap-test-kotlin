package com.acme.transport.fleetops.leasereturn.returnforecast

import kotlin.test.Test
import kotlin.test.assertEquals

class CappedLeaseReturnRollupViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newCappedLeaseReturnRollup("code-713", "label-283")
        val view = toCappedLeaseReturnRollupView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toCappedLeaseReturnRollupViews(mutableListOf<CappedLeaseReturnRollup>(item, item)).size, "views")
    }
}
