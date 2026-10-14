package com.acme.transport.terminal.dockdoor.doorscore

import kotlin.test.Test
import kotlin.test.assertEquals

class PendingDockDoorScoreViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newPendingDockDoorScore("code-405", "title-216")
        val view = toPendingDockDoorScoreView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toPendingDockDoorScoreViews(mutableListOf<PendingDockDoorScore>(item, item)).size, "views")
    }
}
