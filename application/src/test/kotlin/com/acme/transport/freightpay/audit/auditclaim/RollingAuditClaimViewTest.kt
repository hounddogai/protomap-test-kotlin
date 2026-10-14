package com.acme.transport.freightpay.audit.auditclaim

import kotlin.test.Test
import kotlin.test.assertEquals

class RollingAuditClaimViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newRollingAuditClaim("code-918", "name-259")
        val view = toRollingAuditClaimView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toRollingAuditClaimViews(mutableListOf<RollingAuditClaim>(item, item)).size, "views")
    }
}
