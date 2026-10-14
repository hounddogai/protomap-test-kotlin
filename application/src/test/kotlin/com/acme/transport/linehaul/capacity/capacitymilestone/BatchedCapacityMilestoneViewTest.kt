package com.acme.transport.linehaul.capacity.capacitymilestone

import kotlin.test.Test
import kotlin.test.assertEquals

class BatchedCapacityMilestoneViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newBatchedCapacityMilestone("code-870", "label-934")
        val view = toBatchedCapacityMilestoneView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toBatchedCapacityMilestoneViews(mutableListOf<BatchedCapacityMilestone>(item, item)).size, "views")
    }
}
