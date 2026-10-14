package com.acme.transport.freightpay.dispute.disputeassignment

import kotlin.test.Test
import kotlin.test.assertEquals

class CappedDisputeGroupViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newCappedDisputeGroup("code-384", "title-797")
        val view = toCappedDisputeGroupView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toCappedDisputeGroupViews(mutableListOf<CappedDisputeGroup>(item, item)).size, "views")
    }
}
