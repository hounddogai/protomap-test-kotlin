package com.acme.transport.safety.violation.plan

import kotlin.test.Test
import kotlin.test.assertEquals

class ViolationPlanViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newViolationPlan("code-635", "label-668")
        val view = toViolationPlanView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toViolationPlanViews(mutableListOf<ViolationPlan>(item, item)).size, "views")
    }
}
