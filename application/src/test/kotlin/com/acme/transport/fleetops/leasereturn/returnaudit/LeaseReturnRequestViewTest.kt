package com.acme.transport.fleetops.leasereturn.returnaudit

import kotlin.test.Test
import kotlin.test.assertEquals

class LeaseReturnRequestViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newLeaseReturnRequest("code-636", "title-902")
        val view = toLeaseReturnRequestView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toLeaseReturnRequestViews(mutableListOf<LeaseReturnRequest>(item, item)).size, "views")
    }
}
