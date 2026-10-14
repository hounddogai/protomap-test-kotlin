package com.acme.transport.terminal.sortation.milestone

import kotlin.test.Test
import kotlin.test.assertEquals

class SortationWindowViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newSortationWindow("code-189", "title-890")
        val view = toSortationWindowView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toSortationWindowViews(mutableListOf<SortationWindow>(item, item)).size, "views")
    }
}
