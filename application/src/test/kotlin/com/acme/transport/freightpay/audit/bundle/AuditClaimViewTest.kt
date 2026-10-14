package com.acme.transport.freightpay.audit.bundle

import kotlin.test.Test
import kotlin.test.assertEquals

class AuditClaimViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newAuditClaim("code-2", "label-698")
        val view = toAuditClaimView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toAuditClaimViews(mutableListOf<AuditClaim>(item, item)).size, "views")
    }
}
