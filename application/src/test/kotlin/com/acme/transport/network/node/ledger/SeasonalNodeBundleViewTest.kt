package com.acme.transport.network.node.ledger

import kotlin.test.Test
import kotlin.test.assertEquals

class SeasonalNodeBundleViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newSeasonalNodeBundle("code-392", "title-672")
        val view = toSeasonalNodeBundleView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toSeasonalNodeBundleViews(mutableListOf<SeasonalNodeBundle>(item, item)).size, "views")
    }
}
