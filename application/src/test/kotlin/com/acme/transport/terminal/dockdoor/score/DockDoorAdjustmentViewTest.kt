package com.acme.transport.terminal.dockdoor.score

import kotlin.test.Test
import kotlin.test.assertEquals

class DockDoorAdjustmentViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newDockDoorAdjustment("code-389", "title-478")
        val view = toDockDoorAdjustmentView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toDockDoorAdjustmentViews(mutableListOf<DockDoorAdjustment>(item, item)).size, "views")
    }
}
