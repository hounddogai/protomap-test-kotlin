package com.acme.transport.fleetops.leasereturn.entry

import kotlin.test.Test
import kotlin.test.assertEquals

class PrimaryLeaseReturnBudgetViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newPrimaryLeaseReturnBudget("code-597", "label-197")
        val view = toPrimaryLeaseReturnBudgetView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toPrimaryLeaseReturnBudgetViews(mutableListOf<PrimaryLeaseReturnBudget>(item, item)).size, "views")
    }
}
