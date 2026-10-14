package com.acme.transport.freightpay.audit.batch

import kotlin.test.Test
import kotlin.test.assertEquals

class AuditRuleViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newAuditRule("code-34", "title-928")
        val view = toAuditRuleView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toAuditRuleViews(mutableListOf<AuditRule>(item, item)).size, "views")
    }
}
