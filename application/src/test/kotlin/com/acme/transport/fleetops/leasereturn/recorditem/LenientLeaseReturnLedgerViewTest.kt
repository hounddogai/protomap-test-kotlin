package com.acme.transport.fleetops.leasereturn.recorditem

import kotlin.test.Test
import kotlin.test.assertEquals

class LenientLeaseReturnLedgerViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newLenientLeaseReturnLedger("code-910", "label-373")
        val view = toLenientLeaseReturnLedgerView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toLenientLeaseReturnLedgerViews(mutableListOf<LenientLeaseReturnLedger>(item, item)).size, "views")
    }
}
