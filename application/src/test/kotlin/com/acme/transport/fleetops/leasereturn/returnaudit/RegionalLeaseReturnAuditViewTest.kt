package com.acme.transport.fleetops.leasereturn.returnaudit

import kotlin.test.Test
import kotlin.test.assertEquals

class RegionalLeaseReturnAuditViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newRegionalLeaseReturnAudit("code-692", "name-531")
        val view = toRegionalLeaseReturnAuditView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toRegionalLeaseReturnAuditViews(mutableListOf<RegionalLeaseReturnAudit>(item, item)).size, "views")
    }
}
