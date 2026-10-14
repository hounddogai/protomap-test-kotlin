package com.acme.transport.fleetops.leasereturn.mapping

import kotlin.test.Test
import kotlin.test.assertEquals

class RollingLeaseReturnSummaryViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newRollingLeaseReturnSummary("code-497", "title-616")
        val view = toRollingLeaseReturnSummaryView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toRollingLeaseReturnSummaryViews(mutableListOf<RollingLeaseReturnSummary>(item, item)).size, "views")
    }
}
