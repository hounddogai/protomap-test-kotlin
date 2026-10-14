package com.acme.transport.terminal.dockdoor.rollingdoorsnapshot

import kotlin.test.Test
import kotlin.test.assertEquals

class SeasonalDockDoorSnapshotViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newSeasonalDockDoorSnapshot("code-473", "title-608")
        val view = toSeasonalDockDoorSnapshotView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toSeasonalDockDoorSnapshotViews(mutableListOf<SeasonalDockDoorSnapshot>(item, item)).size, "views")
    }
}
