package com.acme.transport.freightpay.audit.budget

import kotlin.test.Test
import kotlin.test.assertEquals

class AuditProfileViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newAuditProfile("code-539", "name-962")
        val view = toAuditProfileView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toAuditProfileViews(mutableListOf<AuditProfile>(item, item)).size, "views")
    }
}
