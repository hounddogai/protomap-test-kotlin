package com.acme.transport.terminal.dockdoor.doorclaim

import kotlin.test.Test
import kotlin.test.assertEquals

class DockDoorBatchViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newDockDoorBatch("code-86", "title-790")
        val view = toDockDoorBatchView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toDockDoorBatchViews(mutableListOf<DockDoorBatch>(item, item)).size, "views")
    }
}
