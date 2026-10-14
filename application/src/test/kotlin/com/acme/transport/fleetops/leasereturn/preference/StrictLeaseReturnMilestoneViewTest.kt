package com.acme.transport.fleetops.leasereturn.preference

import kotlin.test.Test
import kotlin.test.assertEquals

class StrictLeaseReturnMilestoneViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newStrictLeaseReturnMilestone("code-337", "label-455")
        val view = toStrictLeaseReturnMilestoneView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toStrictLeaseReturnMilestoneViews(mutableListOf<StrictLeaseReturnMilestone>(item, item)).size, "views")
    }
}
