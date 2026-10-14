package com.acme.transport.terminal.dockdoor.target

import kotlin.test.Test
import kotlin.test.assertEquals

class DockDoorTargetViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newDockDoorTarget("code-683", "name-995")
        val view = toDockDoorTargetView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toDockDoorTargetViews(mutableListOf<DockDoorTarget>(item, item)).size, "views")
    }
}
