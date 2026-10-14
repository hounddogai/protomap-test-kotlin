package com.acme.transport.terminal.dockdoor.recorditem

import kotlin.test.Test
import kotlin.test.assertEquals

class DockDoorRecordViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newDockDoorRecord("code-857", "title-53")
        val view = toDockDoorRecordView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toDockDoorRecordViews(mutableListOf<DockDoorRecord>(item, item)).size, "views")
    }
}
