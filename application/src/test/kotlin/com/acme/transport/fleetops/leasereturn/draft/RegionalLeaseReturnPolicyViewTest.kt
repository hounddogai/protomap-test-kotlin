package com.acme.transport.fleetops.leasereturn.draft

import kotlin.test.Test
import kotlin.test.assertEquals

class RegionalLeaseReturnPolicyViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newRegionalLeaseReturnPolicy("code-61", "label-510")
        val view = toRegionalLeaseReturnPolicyView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toRegionalLeaseReturnPolicyViews(mutableListOf<RegionalLeaseReturnPolicy>(item, item)).size, "views")
    }
}
