package com.acme.transport.terminal.sortation.tier

import kotlin.test.Test
import kotlin.test.assertEquals

class SortationBatchViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newSortationBatch("code-799", "name-564")
        val view = toSortationBatchView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toSortationBatchViews(mutableListOf<SortationBatch>(item, item)).size, "views")
    }
}
