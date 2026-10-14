package com.acme.transport.terminal.sortation.sortationtier

import kotlin.test.Test
import kotlin.test.assertEquals

class ScheduledSortationTierViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newScheduledSortationTier("code-765", "name-444")
        val view = toScheduledSortationTierView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toScheduledSortationTierViews(mutableListOf<ScheduledSortationTier>(item, item)).size, "views")
    }
}
