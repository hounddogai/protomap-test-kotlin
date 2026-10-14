package com.acme.transport.lastmile.drop.rollup

import kotlin.test.Test
import kotlin.test.assertEquals

class RegionalDropTierViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newRegionalDropTier("code-115", "title-931")
        val view = toRegionalDropTierView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toRegionalDropTierViews(mutableListOf<RegionalDropTier>(item, item)).size, "views")
    }
}
