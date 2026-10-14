package com.acme.transport.freightpay.audit.tier

import kotlin.test.Test
import kotlin.test.assertEquals

class TieredAuditTierViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newTieredAuditTier("code-907", "name-875")
        val view = toTieredAuditTierView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toTieredAuditTierViews(mutableListOf<TieredAuditTier>(item, item)).size, "views")
    }
}
