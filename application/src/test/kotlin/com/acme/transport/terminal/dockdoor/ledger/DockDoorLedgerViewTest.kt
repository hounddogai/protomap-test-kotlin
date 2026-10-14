package com.acme.transport.terminal.dockdoor.ledger

import kotlin.test.Test
import kotlin.test.assertEquals

class DockDoorLedgerViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newDockDoorLedger("code-6", "label-807")
        val view = toDockDoorLedgerView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toDockDoorLedgerViews(mutableListOf<DockDoorLedger>(item, item)).size, "views")
    }
}
