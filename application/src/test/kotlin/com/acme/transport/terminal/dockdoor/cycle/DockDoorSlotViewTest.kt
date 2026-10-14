package com.acme.transport.terminal.dockdoor.cycle

import kotlin.test.Test
import kotlin.test.assertEquals

class DockDoorSlotViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newDockDoorSlot("code-39", "name-295")
        val view = toDockDoorSlotView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toDockDoorSlotViews(mutableListOf<DockDoorSlot>(item, item)).size, "views")
    }
}
