package com.acme.transport.terminal.dockdoor.recorditem

import kotlin.test.Test
import kotlin.test.assertEquals

class SeasonalDockDoorScoreViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newSeasonalDockDoorScore("code-720", "name-595")
        val view = toSeasonalDockDoorScoreView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toSeasonalDockDoorScoreViews(mutableListOf<SeasonalDockDoorScore>(item, item)).size, "views")
    }
}
