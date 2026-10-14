package com.acme.transport.safety.nearmiss.variance

import kotlin.test.Test
import kotlin.test.assertEquals

class BatchedNearMissSegmentViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newBatchedNearMissSegment("code-267", "name-703")
        val view = toBatchedNearMissSegmentView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toBatchedNearMissSegmentViews(mutableListOf<BatchedNearMissSegment>(item, item)).size, "views")
    }
}
