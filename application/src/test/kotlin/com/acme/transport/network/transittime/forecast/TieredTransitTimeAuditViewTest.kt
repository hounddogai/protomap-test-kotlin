package com.acme.transport.network.transittime.forecast

import kotlin.test.Test
import kotlin.test.assertEquals

class TieredTransitTimeAuditViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newTieredTransitTimeAudit("code-50", "title-7")
        val view = toTieredTransitTimeAuditView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toTieredTransitTimeAuditViews(mutableListOf<TieredTransitTimeAudit>(item, item)).size, "views")
    }
}
