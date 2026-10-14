package com.acme.transport.freightpay.audit.notice

import kotlin.test.Test
import kotlin.test.assertEquals

class AuditTargetViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newAuditTarget("code-544", "name-475")
        val view = toAuditTargetView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toAuditTargetViews(mutableListOf<AuditTarget>(item, item)).size, "views")
    }
}
