package com.acme.transport.terminal.dockdoor.score

import kotlin.test.Test
import kotlin.test.assertEquals

class DockDoorSummaryViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newDockDoorSummary("code-71", "name-638")
        val view = toDockDoorSummaryView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toDockDoorSummaryViews(mutableListOf<DockDoorSummary>(item, item)).size, "views")
    }
}
