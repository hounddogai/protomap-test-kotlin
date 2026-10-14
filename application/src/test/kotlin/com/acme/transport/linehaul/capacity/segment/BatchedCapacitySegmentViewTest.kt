package com.acme.transport.linehaul.capacity.segment

import kotlin.test.Test
import kotlin.test.assertEquals

class BatchedCapacitySegmentViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newBatchedCapacitySegment("code-119", "name-656")
        val view = toBatchedCapacitySegmentView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toBatchedCapacitySegmentViews(mutableListOf<BatchedCapacitySegment>(item, item)).size, "views")
    }
}
