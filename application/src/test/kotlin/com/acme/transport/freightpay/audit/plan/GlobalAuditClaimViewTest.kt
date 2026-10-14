package com.acme.transport.freightpay.audit.plan

import kotlin.test.Test
import kotlin.test.assertEquals

class GlobalAuditClaimViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newGlobalAuditClaim("code-416", "label-574")
        val view = toGlobalAuditClaimView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toGlobalAuditClaimViews(mutableListOf<GlobalAuditClaim>(item, item)).size, "views")
    }
}
