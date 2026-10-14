package com.acme.transport.terminal.dockdoor.schedule

import kotlin.test.Test
import kotlin.test.assertEquals

class PendingDockDoorDraftViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newPendingDockDoorDraft("code-659", "title-366")
        val view = toPendingDockDoorDraftView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toPendingDockDoorDraftViews(mutableListOf<PendingDockDoorDraft>(item, item)).size, "views")
    }
}
