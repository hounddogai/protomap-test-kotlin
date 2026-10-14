package com.acme.transport.freightpay.dispute.slot

import kotlin.test.Test
import kotlin.test.assertEquals

class FallbackDisputePlanViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newFallbackDisputePlan("code-168", "label-624")
        val view = toFallbackDisputePlanView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toFallbackDisputePlanViews(mutableListOf<FallbackDisputePlan>(item, item)).size, "views")
    }
}
