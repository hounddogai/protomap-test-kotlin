package com.acme.transport.fleetops.leasereturn.tier

import kotlin.test.Test
import kotlin.test.assertEquals

class LeaseReturnWindowViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newLeaseReturnWindow("code-980", "label-103")
        val view = toLeaseReturnWindowView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toLeaseReturnWindowViews(mutableListOf<LeaseReturnWindow>(item, item)).size, "views")
    }
}
