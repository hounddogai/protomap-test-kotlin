package com.acme.transport.freightpay.dispute.manifest

import kotlin.test.Test
import kotlin.test.assertEquals

class StrictDisputePlanViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newStrictDisputePlan("code-766", "title-425")
        val view = toStrictDisputePlanView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toStrictDisputePlanViews(mutableListOf<StrictDisputePlan>(item, item)).size, "views")
    }
}
