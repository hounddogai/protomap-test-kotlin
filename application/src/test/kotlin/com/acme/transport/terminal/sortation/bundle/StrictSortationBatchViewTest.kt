package com.acme.transport.terminal.sortation.bundle

import kotlin.test.Test
import kotlin.test.assertEquals

class StrictSortationBatchViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newStrictSortationBatch("code-811", "title-985")
        val view = toStrictSortationBatchView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toStrictSortationBatchViews(mutableListOf<StrictSortationBatch>(item, item)).size, "views")
    }
}
