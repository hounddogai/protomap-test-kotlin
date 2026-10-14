package com.acme.transport.freightpay.dispute.policy

import kotlin.test.Test
import kotlin.test.assertEquals

class DisputePolicyViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newDisputePolicy("code-397", "label-957")
        val view = toDisputePolicyView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toDisputePolicyViews(mutableListOf<DisputePolicy>(item, item)).size, "views")
    }
}
