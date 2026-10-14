package com.acme.transport.freightpay.audit.auditclaim

import kotlin.test.Test
import kotlin.test.assertEquals

class CappedAuditTargetViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newCappedAuditTarget("code-490", "name-299")
        val view = toCappedAuditTargetView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toCappedAuditTargetViews(mutableListOf<CappedAuditTarget>(item, item)).size, "views")
    }
}
