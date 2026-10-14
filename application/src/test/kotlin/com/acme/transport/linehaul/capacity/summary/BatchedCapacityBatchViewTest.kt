package com.acme.transport.linehaul.capacity.summary

import kotlin.test.Test
import kotlin.test.assertEquals

class BatchedCapacityBatchViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newBatchedCapacityBatch("code-170", "title-598")
        val view = toBatchedCapacityBatchView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toBatchedCapacityBatchViews(mutableListOf<BatchedCapacityBatch>(item, item)).size, "views")
    }
}
