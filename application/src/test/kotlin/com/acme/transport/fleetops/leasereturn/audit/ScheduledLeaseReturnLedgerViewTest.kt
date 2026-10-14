package com.acme.transport.fleetops.leasereturn.audit

import kotlin.test.Test
import kotlin.test.assertEquals

class ScheduledLeaseReturnLedgerViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newScheduledLeaseReturnLedger("code-315", "label-916")
        val view = toScheduledLeaseReturnLedgerView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toScheduledLeaseReturnLedgerViews(mutableListOf<ScheduledLeaseReturnLedger>(item, item)).size, "views")
    }
}
