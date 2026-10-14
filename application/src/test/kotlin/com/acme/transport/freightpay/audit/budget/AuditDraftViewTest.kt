package com.acme.transport.freightpay.audit.budget

import kotlin.test.Test
import kotlin.test.assertEquals

class AuditDraftViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newAuditDraft("code-225", "name-122")
        val view = toAuditDraftView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toAuditDraftViews(mutableListOf<AuditDraft>(item, item)).size, "views")
    }
}
