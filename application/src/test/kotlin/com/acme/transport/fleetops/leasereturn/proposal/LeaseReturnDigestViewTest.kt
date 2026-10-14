package com.acme.transport.fleetops.leasereturn.proposal

import kotlin.test.Test
import kotlin.test.assertEquals

class LeaseReturnDigestViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newLeaseReturnDigest("code-227", "name-688")
        val view = toLeaseReturnDigestView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toLeaseReturnDigestViews(mutableListOf<LeaseReturnDigest>(item, item)).size, "views")
    }
}
