package com.acme.transport.freightpay.dispute.disputepolicy

import kotlin.test.Test
import kotlin.test.assertEquals

class CappedDisputePolicyViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newCappedDisputePolicy("code-834", "title-45")
        val view = toCappedDisputePolicyView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toCappedDisputePolicyViews(mutableListOf<CappedDisputePolicy>(item, item)).size, "views")
    }
}
