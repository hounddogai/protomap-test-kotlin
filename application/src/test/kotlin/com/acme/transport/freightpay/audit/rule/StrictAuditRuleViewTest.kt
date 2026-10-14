package com.acme.transport.freightpay.audit.rule

import kotlin.test.Test
import kotlin.test.assertEquals

class StrictAuditRuleViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newStrictAuditRule("code-925", "label-459")
        val view = toStrictAuditRuleView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toStrictAuditRuleViews(mutableListOf<StrictAuditRule>(item, item)).size, "views")
    }
}
