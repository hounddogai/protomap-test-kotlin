package com.acme.transport.fleetops.leasereturn.adaptivereturnledger

import kotlin.test.Test
import kotlin.test.assertEquals

class LeaseReturnClaimViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newLeaseReturnClaim("code-693", "title-59")
        val view = toLeaseReturnClaimView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toLeaseReturnClaimViews(mutableListOf<LeaseReturnClaim>(item, item)).size, "views")
    }
}
