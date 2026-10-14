package com.acme.transport.freightpay.audit.policy

import kotlin.test.Test
import kotlin.test.assertEquals

class AuditPolicyViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newAuditPolicy("code-115", "title-156")
        val view = toAuditPolicyView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toAuditPolicyViews(mutableListOf<AuditPolicy>(item, item)).size, "views")
    }
}
