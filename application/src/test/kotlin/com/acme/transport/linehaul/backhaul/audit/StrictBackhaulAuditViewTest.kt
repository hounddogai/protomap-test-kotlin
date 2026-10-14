package com.acme.transport.linehaul.backhaul.audit

import kotlin.test.Test
import kotlin.test.assertEquals

class StrictBackhaulAuditViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newStrictBackhaulAudit("code-39", "title-191")
        val view = toStrictBackhaulAuditView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toStrictBackhaulAuditViews(mutableListOf<StrictBackhaulAudit>(item, item)).size, "views")
    }
}
