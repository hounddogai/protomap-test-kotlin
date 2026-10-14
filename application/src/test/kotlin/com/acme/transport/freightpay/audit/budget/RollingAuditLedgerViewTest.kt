package com.acme.transport.freightpay.audit.budget

import kotlin.test.Test
import kotlin.test.assertEquals

class RollingAuditLedgerViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newRollingAuditLedger("code-5", "name-819")
        val view = toRollingAuditLedgerView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toRollingAuditLedgerViews(mutableListOf<RollingAuditLedger>(item, item)).size, "views")
    }
}
