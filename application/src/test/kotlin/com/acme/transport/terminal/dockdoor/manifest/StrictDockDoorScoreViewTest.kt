package com.acme.transport.terminal.dockdoor.manifest

import kotlin.test.Test
import kotlin.test.assertEquals

class StrictDockDoorScoreViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newStrictDockDoorScore("code-724", "label-92")
        val view = toStrictDockDoorScoreView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toStrictDockDoorScoreViews(mutableListOf<StrictDockDoorScore>(item, item)).size, "views")
    }
}
