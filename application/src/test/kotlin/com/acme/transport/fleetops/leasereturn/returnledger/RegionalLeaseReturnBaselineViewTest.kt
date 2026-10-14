package com.acme.transport.fleetops.leasereturn.returnledger

import kotlin.test.Test
import kotlin.test.assertEquals

class RegionalLeaseReturnBaselineViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newRegionalLeaseReturnBaseline("code-423", "name-902")
        val view = toRegionalLeaseReturnBaselineView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toRegionalLeaseReturnBaselineViews(mutableListOf<RegionalLeaseReturnBaseline>(item, item)).size, "views")
    }
}
