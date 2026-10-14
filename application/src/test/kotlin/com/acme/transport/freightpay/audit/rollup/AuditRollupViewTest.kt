package com.acme.transport.freightpay.audit.rollup

import kotlin.test.Test
import kotlin.test.assertEquals

class AuditRollupViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newAuditRollup("code-618", "title-505")
        val view = toAuditRollupView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toAuditRollupViews(mutableListOf<AuditRollup>(item, item)).size, "views")
    }
}
