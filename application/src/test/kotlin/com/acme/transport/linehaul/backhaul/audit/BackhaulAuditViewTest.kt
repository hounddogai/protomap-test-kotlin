package com.acme.transport.linehaul.backhaul.audit

import kotlin.test.Test
import kotlin.test.assertEquals

class BackhaulAuditViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newBackhaulAudit("code-277", "label-445")
        val view = toBackhaulAuditView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toBackhaulAuditViews(mutableListOf<BackhaulAudit>(item, item)).size, "views")
    }
}
