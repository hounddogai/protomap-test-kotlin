package com.acme.transport.terminal.dockdoor.proposal

import kotlin.test.Test
import kotlin.test.assertEquals

class SeasonalDockDoorTierViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newSeasonalDockDoorTier("code-35", "name-664")
        val view = toSeasonalDockDoorTierView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toSeasonalDockDoorTierViews(mutableListOf<SeasonalDockDoorTier>(item, item)).size, "views")
    }
}
