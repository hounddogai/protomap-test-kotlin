package com.acme.transport.freightpay.audit.plan

import kotlin.test.Test
import kotlin.test.assertEquals

class AuditPlanViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newAuditPlan("code-263", "name-707")
        val view = toAuditPlanView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toAuditPlanViews(mutableListOf<AuditPlan>(item, item)).size, "views")
    }
}
