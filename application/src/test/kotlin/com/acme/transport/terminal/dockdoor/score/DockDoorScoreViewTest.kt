package com.acme.transport.terminal.dockdoor.score

import kotlin.test.Test
import kotlin.test.assertEquals

class DockDoorScoreViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newDockDoorScore("code-795", "name-104")
        val view = toDockDoorScoreView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toDockDoorScoreViews(mutableListOf<DockDoorScore>(item, item)).size, "views")
    }
}
