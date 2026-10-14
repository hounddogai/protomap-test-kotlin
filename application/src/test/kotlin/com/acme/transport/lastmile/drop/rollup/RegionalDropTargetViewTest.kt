package com.acme.transport.lastmile.drop.rollup

import kotlin.test.Test
import kotlin.test.assertEquals

class RegionalDropTargetViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newRegionalDropTarget("code-825", "title-683")
        val view = toRegionalDropTargetView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toRegionalDropTargetViews(mutableListOf<RegionalDropTarget>(item, item)).size, "views")
    }
}
