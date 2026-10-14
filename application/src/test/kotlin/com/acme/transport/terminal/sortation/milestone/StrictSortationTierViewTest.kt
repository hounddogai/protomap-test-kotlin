package com.acme.transport.terminal.sortation.milestone

import kotlin.test.Test
import kotlin.test.assertEquals

class StrictSortationTierViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newStrictSortationTier("code-729", "title-288")
        val view = toStrictSortationTierView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toStrictSortationTierViews(mutableListOf<StrictSortationTier>(item, item)).size, "views")
    }
}
