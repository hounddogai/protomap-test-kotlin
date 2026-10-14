package com.acme.transport.terminal.sortation.preference

import kotlin.test.Test
import kotlin.test.assertEquals

class SortationAllocationViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newSortationAllocation("code-176", "name-204")
        val view = toSortationAllocationView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toSortationAllocationViews(mutableListOf<SortationAllocation>(item, item)).size, "views")
    }
}
