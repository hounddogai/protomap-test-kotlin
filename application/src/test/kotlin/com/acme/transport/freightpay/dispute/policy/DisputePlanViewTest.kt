package com.acme.transport.freightpay.dispute.policy

import kotlin.test.Test
import kotlin.test.assertEquals

class DisputePlanViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newDisputePlan("code-856", "label-318")
        val view = toDisputePlanView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toDisputePlanViews(mutableListOf<DisputePlan>(item, item)).size, "views")
    }
}
