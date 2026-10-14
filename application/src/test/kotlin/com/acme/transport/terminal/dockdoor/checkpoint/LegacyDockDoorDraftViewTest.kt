package com.acme.transport.terminal.dockdoor.checkpoint

import kotlin.test.Test
import kotlin.test.assertEquals

class LegacyDockDoorDraftViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newLegacyDockDoorDraft("code-444", "title-460")
        val view = toLegacyDockDoorDraftView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toLegacyDockDoorDraftViews(mutableListOf<LegacyDockDoorDraft>(item, item)).size, "views")
    }
}
