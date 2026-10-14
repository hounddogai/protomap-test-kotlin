package com.acme.transport.terminal.dockdoor.doorsnapshot

import kotlin.test.Test
import kotlin.test.assertEquals

class LenientDockDoorSlotViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newLenientDockDoorSlot("code-699", "label-377")
        val view = toLenientDockDoorSlotView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toLenientDockDoorSlotViews(mutableListOf<LenientDockDoorSlot>(item, item)).size, "views")
    }
}
