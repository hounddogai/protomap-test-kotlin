package com.acme.transport.terminal.sortation.tier

import kotlin.test.Test
import kotlin.test.assertEquals

class SortationTierViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newSortationTier("code-326", "label-991")
        val view = toSortationTierView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toSortationTierViews(mutableListOf<SortationTier>(item, item)).size, "views")
    }
}
