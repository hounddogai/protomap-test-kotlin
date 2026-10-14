package com.acme.transport.terminal.dockdoor.doorsnapshot

import kotlin.test.Test
import kotlin.test.assertEquals

class CappedDockDoorSnapshotViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newCappedDockDoorSnapshot("code-398", "label-279")
        val view = toCappedDockDoorSnapshotView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toCappedDockDoorSnapshotViews(mutableListOf<CappedDockDoorSnapshot>(item, item)).size, "views")
    }
}
