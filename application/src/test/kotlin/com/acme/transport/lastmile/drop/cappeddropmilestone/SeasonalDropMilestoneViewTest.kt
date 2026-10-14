package com.acme.transport.lastmile.drop.cappeddropmilestone

import kotlin.test.Test
import kotlin.test.assertEquals

class SeasonalDropMilestoneViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newSeasonalDropMilestone("code-834", "label-292")
        val view = toSeasonalDropMilestoneView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toSeasonalDropMilestoneViews(mutableListOf<SeasonalDropMilestone>(item, item)).size, "views")
    }
}
