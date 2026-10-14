package com.acme.transport.linehaul.tender.tenderestimate

import kotlin.test.Test
import kotlin.test.assertEquals

class TenderAuditViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newTenderAudit("code-442", "title-397")
        val view = toTenderAuditView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toTenderAuditViews(mutableListOf<TenderAudit>(item, item)).size, "views")
    }
}
