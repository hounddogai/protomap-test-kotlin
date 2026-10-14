package com.acme.transport.terminal.dockdoor.variance

import kotlin.test.Test
import kotlin.test.assertEquals

class SeasonalDockDoorAdjustmentViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newSeasonalDockDoorAdjustment("code-859", "label-370")
        val view = toSeasonalDockDoorAdjustmentView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toSeasonalDockDoorAdjustmentViews(mutableListOf<SeasonalDockDoorAdjustment>(item, item)).size, "views")
    }
}
