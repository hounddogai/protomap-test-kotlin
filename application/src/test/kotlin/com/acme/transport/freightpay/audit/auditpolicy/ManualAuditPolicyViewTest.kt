package com.acme.transport.freightpay.audit.auditpolicy

import kotlin.test.Test
import kotlin.test.assertEquals

class ManualAuditPolicyViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newManualAuditPolicy("code-617", "title-123")
        val view = toManualAuditPolicyView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toManualAuditPolicyViews(mutableListOf<ManualAuditPolicy>(item, item)).size, "views")
    }
}
