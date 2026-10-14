package com.acme.transport.lastmile.drop.dropreservation

import kotlin.test.Test
import kotlin.test.assertEquals

class StrictDropAuditViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newStrictDropAudit("code-712", "title-517")
        val view = toStrictDropAuditView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toStrictDropAuditViews(mutableListOf<StrictDropAudit>(item, item)).size, "views")
    }
}
