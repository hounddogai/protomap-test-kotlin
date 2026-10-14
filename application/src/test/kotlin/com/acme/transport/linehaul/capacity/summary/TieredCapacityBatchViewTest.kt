package com.acme.transport.linehaul.capacity.summary

import kotlin.test.Test
import kotlin.test.assertEquals

class TieredCapacityBatchViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newTieredCapacityBatch("code-850", "title-123")
        val view = toTieredCapacityBatchView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toTieredCapacityBatchViews(mutableListOf<TieredCapacityBatch>(item, item)).size, "views")
    }
}
