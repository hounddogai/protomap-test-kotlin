package com.acme.transport.fleetops.leasereturn.baseline

import kotlin.test.Test
import kotlin.test.assertEquals

class StrictLeaseReturnBaselineViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newStrictLeaseReturnBaseline("code-901", "label-59")
        val view = toStrictLeaseReturnBaselineView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toStrictLeaseReturnBaselineViews(mutableListOf<StrictLeaseReturnBaseline>(item, item)).size, "views")
    }
}
