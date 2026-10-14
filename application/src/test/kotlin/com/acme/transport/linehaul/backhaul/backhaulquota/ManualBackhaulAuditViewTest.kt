package com.acme.transport.linehaul.backhaul.backhaulquota

import kotlin.test.Test
import kotlin.test.assertEquals

class ManualBackhaulAuditViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newManualBackhaulAudit("code-638", "label-451")
        val view = toManualBackhaulAuditView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toManualBackhaulAuditViews(mutableListOf<ManualBackhaulAudit>(item, item)).size, "views")
    }
}
