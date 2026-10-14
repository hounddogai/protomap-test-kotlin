package com.acme.transport.terminal.dockdoor.target

import kotlin.test.Test
import kotlin.test.assertEquals

class ScheduledDockDoorDraftViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newScheduledDockDoorDraft("code-357", "title-911")
        val view = toScheduledDockDoorDraftView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toScheduledDockDoorDraftViews(mutableListOf<ScheduledDockDoorDraft>(item, item)).size, "views")
    }
}
