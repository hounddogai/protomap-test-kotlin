package com.acme.transport.terminal.dockdoor.variance

import kotlin.test.Test
import kotlin.test.assertEquals

class PendingDockDoorTierViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newPendingDockDoorTier("code-553", "label-903")
        val view = toPendingDockDoorTierView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toPendingDockDoorTierViews(mutableListOf<PendingDockDoorTier>(item, item)).size, "views")
    }
}
