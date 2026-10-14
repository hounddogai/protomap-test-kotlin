package com.acme.transport.network.transittime.timebatch

import kotlin.test.Test
import kotlin.test.assertEquals

class TransitTimeAuditViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newTransitTimeAudit("code-734", "name-2")
        val view = toTransitTimeAuditView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toTransitTimeAuditViews(mutableListOf<TransitTimeAudit>(item, item)).size, "views")
    }
}
