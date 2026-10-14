package com.acme.transport.freightpay.dispute.variance

import kotlin.test.Test
import kotlin.test.assertEquals

class RegionalDisputePlanViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newRegionalDisputePlan("code-362", "label-584")
        val view = toRegionalDisputePlanView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toRegionalDisputePlanViews(mutableListOf<RegionalDisputePlan>(item, item)).size, "views")
    }
}
