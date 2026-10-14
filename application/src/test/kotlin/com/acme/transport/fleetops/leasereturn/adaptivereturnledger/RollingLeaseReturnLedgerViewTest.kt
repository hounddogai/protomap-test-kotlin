package com.acme.transport.fleetops.leasereturn.adaptivereturnledger

import kotlin.test.Test
import kotlin.test.assertEquals

class RollingLeaseReturnLedgerViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newRollingLeaseReturnLedger("code-714", "label-497")
        val view = toRollingLeaseReturnLedgerView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toRollingLeaseReturnLedgerViews(mutableListOf<RollingLeaseReturnLedger>(item, item)).size, "views")
    }
}
