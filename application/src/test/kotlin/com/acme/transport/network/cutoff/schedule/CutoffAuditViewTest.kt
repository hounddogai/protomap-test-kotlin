package com.acme.transport.network.cutoff.schedule

import kotlin.test.Test
import kotlin.test.assertEquals

class CutoffAuditViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newCutoffAudit("code-725", "title-211")
        val view = toCutoffAuditView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toCutoffAuditViews(mutableListOf<CutoffAudit>(item, item)).size, "views")
    }
}
