package com.acme.transport.linehaul.capacity.capacityrequest

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class BatchedCapacityScheduleTest {
    @Test
    fun `starts in draft`() {
        val item = newBatchedCapacitySchedule("code-769", "title-502")
        assertEquals(BatchedCapacityScheduleStatus.DRAFT, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to pending`() {
        val item = newBatchedCapacitySchedule("code-769", "title-502")
        try {
            item.moveTo(BatchedCapacityScheduleStatus.PENDING)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(BatchedCapacityScheduleStatus.PENDING, item.status, "status")
    }

    @Test
    fun `rejects moves from closed`() {
        val item = newBatchedCapacitySchedule("code-769", "title-502")
        item.status = BatchedCapacityScheduleStatus.CLOSED
        assertFails("move") { item.moveTo(BatchedCapacityScheduleStatus.DRAFT) }
    }
}
