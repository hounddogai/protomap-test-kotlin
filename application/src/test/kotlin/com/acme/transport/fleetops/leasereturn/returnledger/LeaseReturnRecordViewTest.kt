package com.acme.transport.fleetops.leasereturn.returnledger

import kotlin.test.Test
import kotlin.test.assertEquals

class LeaseReturnRecordViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newLeaseReturnRecord("code-273", "title-590")
        val view = toLeaseReturnRecordView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toLeaseReturnRecordViews(mutableListOf<LeaseReturnRecord>(item, item)).size, "views")
    }
}
