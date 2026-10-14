package com.acme.transport.fleetops.leasereturn.draft

import kotlin.test.Test
import kotlin.test.assertEquals

class RegionalLeaseReturnVarianceViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newRegionalLeaseReturnVariance("code-219", "title-417")
        val view = toRegionalLeaseReturnVarianceView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toRegionalLeaseReturnVarianceViews(mutableListOf<RegionalLeaseReturnVariance>(item, item)).size, "views")
    }
}
