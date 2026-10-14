package com.acme.transport.lastmile.redelivery.slot

import kotlin.test.Test
import kotlin.test.assertEquals

class RedeliveryPlanViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newRedeliveryPlan("code-759", "label-98")
        val view = toRedeliveryPlanView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toRedeliveryPlanViews(mutableListOf<RedeliveryPlan>(item, item)).size, "views")
    }
}
