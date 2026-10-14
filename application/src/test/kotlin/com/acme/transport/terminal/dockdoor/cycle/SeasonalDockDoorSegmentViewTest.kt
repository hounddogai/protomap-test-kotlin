package com.acme.transport.terminal.dockdoor.cycle

import kotlin.test.Test
import kotlin.test.assertEquals

class SeasonalDockDoorSegmentViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newSeasonalDockDoorSegment("code-965", "title-257")
        val view = toSeasonalDockDoorSegmentView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toSeasonalDockDoorSegmentViews(mutableListOf<SeasonalDockDoorSegment>(item, item)).size, "views")
    }
}
