package com.acme.transport.terminal.dockdoor.slot

import kotlin.test.Test
import kotlin.test.assertEquals

class PrimaryDockDoorAuditViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newPrimaryDockDoorAudit("code-357", "label-314")
        val view = toPrimaryDockDoorAuditView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toPrimaryDockDoorAuditViews(mutableListOf<PrimaryDockDoorAudit>(item, item)).size, "views")
    }
}
