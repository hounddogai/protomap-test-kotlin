package com.acme.transport.linehaul.capacity.plan

import kotlin.test.Test
import kotlin.test.assertEquals

class CapacityPlanViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newCapacityPlan("code-488", "label-2")
        val view = toCapacityPlanView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toCapacityPlanViews(mutableListOf<CapacityPlan>(item, item)).size, "views")
    }
}
