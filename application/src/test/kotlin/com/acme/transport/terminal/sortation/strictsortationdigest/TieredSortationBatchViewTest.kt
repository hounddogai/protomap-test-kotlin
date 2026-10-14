package com.acme.transport.terminal.sortation.strictsortationdigest

import kotlin.test.Test
import kotlin.test.assertEquals

class TieredSortationBatchViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newTieredSortationBatch("code-223", "name-950")
        val view = toTieredSortationBatchView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toTieredSortationBatchViews(mutableListOf<TieredSortationBatch>(item, item)).size, "views")
    }
}
