package com.acme.transport.fleetops.leasereturn.recorditem

import kotlin.test.Test
import kotlin.test.assertEquals

class LeaseReturnReviewViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newLeaseReturnReview("code-312", "title-798")
        val view = toLeaseReturnReviewView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toLeaseReturnReviewViews(mutableListOf<LeaseReturnReview>(item, item)).size, "views")
    }
}
