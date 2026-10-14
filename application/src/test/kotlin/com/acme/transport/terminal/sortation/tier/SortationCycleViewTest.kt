package com.acme.transport.terminal.sortation.tier

import kotlin.test.Test
import kotlin.test.assertEquals

class SortationCycleViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newSortationCycle("code-52", "title-568")
        val view = toSortationCycleView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toSortationCycleViews(mutableListOf<SortationCycle>(item, item)).size, "views")
    }
}
