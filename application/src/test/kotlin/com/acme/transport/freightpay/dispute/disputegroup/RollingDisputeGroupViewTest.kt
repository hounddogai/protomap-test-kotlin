package com.acme.transport.freightpay.dispute.disputegroup

import kotlin.test.Test
import kotlin.test.assertEquals

class RollingDisputeGroupViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newRollingDisputeGroup("code-929", "title-430")
        val view = toRollingDisputeGroupView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toRollingDisputeGroupViews(mutableListOf<RollingDisputeGroup>(item, item)).size, "views")
    }
}
