package com.acme.transport.fleetops.leasereturn.forecast

import kotlin.test.Test
import kotlin.test.assertEquals

class WeightedLeaseReturnProfileViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newWeightedLeaseReturnProfile("code-800", "label-821")
        val view = toWeightedLeaseReturnProfileView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toWeightedLeaseReturnProfileViews(mutableListOf<WeightedLeaseReturnProfile>(item, item)).size, "views")
    }
}
