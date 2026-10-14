package com.acme.transport.fleetops.leasereturn.bundle

import kotlin.test.Test
import kotlin.test.assertEquals

class LeaseReturnBundleViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newLeaseReturnBundle("code-983", "name-899")
        val view = toLeaseReturnBundleView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toLeaseReturnBundleViews(mutableListOf<LeaseReturnBundle>(item, item)).size, "views")
    }
}
