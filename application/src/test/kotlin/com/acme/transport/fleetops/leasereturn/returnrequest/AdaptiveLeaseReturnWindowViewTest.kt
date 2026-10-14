package com.acme.transport.fleetops.leasereturn.returnrequest

import kotlin.test.Test
import kotlin.test.assertEquals

class AdaptiveLeaseReturnWindowViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newAdaptiveLeaseReturnWindow("code-73", "name-459")
        val view = toAdaptiveLeaseReturnWindowView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toAdaptiveLeaseReturnWindowViews(mutableListOf<AdaptiveLeaseReturnWindow>(item, item)).size, "views")
    }
}
