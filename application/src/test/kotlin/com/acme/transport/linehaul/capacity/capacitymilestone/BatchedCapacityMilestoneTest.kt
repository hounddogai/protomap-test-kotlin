package com.acme.transport.linehaul.capacity.capacitymilestone

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class BatchedCapacityMilestoneTest {
    @Test
    fun `starts in open`() {
        val item = newBatchedCapacityMilestone("code-870", "label-934")
        assertEquals(BatchedCapacityMilestoneStatus.OPEN, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to resolved`() {
        val item = newBatchedCapacityMilestone("code-870", "label-934")
        try {
            item.moveTo(BatchedCapacityMilestoneStatus.RESOLVED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(BatchedCapacityMilestoneStatus.RESOLVED, item.status, "status")
    }

    @Test
    fun `rejects moves from archived`() {
        val item = newBatchedCapacityMilestone("code-870", "label-934")
        item.status = BatchedCapacityMilestoneStatus.ARCHIVED
        assertFails("move") { item.moveTo(BatchedCapacityMilestoneStatus.OPEN) }
    }
}
