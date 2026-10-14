package com.acme.transport.terminal.dockdoor.agreement

import kotlin.test.Test
import kotlin.test.assertEquals

class DockDoorSnapshotViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newDockDoorSnapshot("code-883", "name-611")
        val view = toDockDoorSnapshotView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toDockDoorSnapshotViews(mutableListOf<DockDoorSnapshot>(item, item)).size, "views")
    }
}
