package com.acme.transport.freightpay.audit.auditoverride

import kotlin.test.Test
import kotlin.test.assertEquals

class TieredAuditOverrideViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newTieredAuditOverride("code-372", "title-955")
        val view = toTieredAuditOverrideView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toTieredAuditOverrideViews(mutableListOf<TieredAuditOverride>(item, item)).size, "views")
    }
}
