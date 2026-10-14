package com.acme.transport.fleetops.leasereturn.segment

import kotlin.test.Test
import kotlin.test.assertEquals

class LeaseReturnSegmentViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newLeaseReturnSegment("code-757", "label-959")
        val view = toLeaseReturnSegmentView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toLeaseReturnSegmentViews(mutableListOf<LeaseReturnSegment>(item, item)).size, "views")
    }
}
