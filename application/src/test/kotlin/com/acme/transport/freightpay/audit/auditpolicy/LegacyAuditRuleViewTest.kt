package com.acme.transport.freightpay.audit.auditpolicy

import kotlin.test.Test
import kotlin.test.assertEquals

class LegacyAuditRuleViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newLegacyAuditRule("code-864", "name-672")
        val view = toLegacyAuditRuleView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toLegacyAuditRuleViews(mutableListOf<LegacyAuditRule>(item, item)).size, "views")
    }
}
