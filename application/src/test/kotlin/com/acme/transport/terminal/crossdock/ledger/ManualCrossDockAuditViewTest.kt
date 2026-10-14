package com.acme.transport.terminal.crossdock.ledger

import kotlin.test.Test
import kotlin.test.assertEquals

class ManualCrossDockAuditViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newManualCrossDockAudit("code-826", "name-448")
        val view = toManualCrossDockAuditView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toManualCrossDockAuditViews(mutableListOf<ManualCrossDockAudit>(item, item)).size, "views")
    }
}
