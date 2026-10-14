package com.acme.transport.terminal.dockdoor.doorrecord

import kotlin.test.Test
import kotlin.test.assertEquals

class StrictDockDoorWindowViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newStrictDockDoorWindow("code-650", "name-834")
        val view = toStrictDockDoorWindowView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toStrictDockDoorWindowViews(mutableListOf<StrictDockDoorWindow>(item, item)).size, "views")
    }
}
