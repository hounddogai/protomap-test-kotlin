package com.acme.transport.network.transittime.timedigest

import kotlin.test.Test
import kotlin.test.assertEquals

class LenientTransitTimeAuditViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newLenientTransitTimeAudit("code-850", "title-934")
        val view = toLenientTransitTimeAuditView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toLenientTransitTimeAuditViews(mutableListOf<LenientTransitTimeAudit>(item, item)).size, "views")
    }
}
