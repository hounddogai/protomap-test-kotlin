package com.acme.transport.terminal.dockdoor.rollingdoorsnapshot

import kotlin.test.Test
import kotlin.test.assertEquals

class GlobalDockDoorSummaryViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newGlobalDockDoorSummary("code-697", "label-541")
        val view = toGlobalDockDoorSummaryView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toGlobalDockDoorSummaryViews(mutableListOf<GlobalDockDoorSummary>(item, item)).size, "views")
    }
}
