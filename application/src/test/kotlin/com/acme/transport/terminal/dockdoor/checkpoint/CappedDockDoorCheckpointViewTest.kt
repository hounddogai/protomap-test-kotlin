package com.acme.transport.terminal.dockdoor.checkpoint

import kotlin.test.Test
import kotlin.test.assertEquals

class CappedDockDoorCheckpointViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newCappedDockDoorCheckpoint("code-331", "title-457")
        val view = toCappedDockDoorCheckpointView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toCappedDockDoorCheckpointViews(mutableListOf<CappedDockDoorCheckpoint>(item, item)).size, "views")
    }
}
