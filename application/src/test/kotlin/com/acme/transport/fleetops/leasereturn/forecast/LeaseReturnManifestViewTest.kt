package com.acme.transport.fleetops.leasereturn.forecast

import kotlin.test.Test
import kotlin.test.assertEquals

class LeaseReturnManifestViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newLeaseReturnManifest("code-650", "label-550")
        val view = toLeaseReturnManifestView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toLeaseReturnManifestViews(mutableListOf<LeaseReturnManifest>(item, item)).size, "views")
    }
}
