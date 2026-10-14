package com.acme.transport.fleetops.leasereturn.budget

import kotlin.test.Test
import kotlin.test.assertEquals

class LeaseReturnEntryViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newLeaseReturnEntry("code-751", "title-707")
        val view = toLeaseReturnEntryView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toLeaseReturnEntryViews(mutableListOf<LeaseReturnEntry>(item, item)).size, "views")
    }
}
