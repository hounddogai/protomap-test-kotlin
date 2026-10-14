package com.acme.transport.customs.hold.holdsummary

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class BatchedHoldScheduleTest {
    @Test
    fun `starts in proposed`() {
        val item = newBatchedHoldSchedule("code-889", "label-491")
        assertEquals(BatchedHoldScheduleStatus.PROPOSED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to approved`() {
        val item = newBatchedHoldSchedule("code-889", "label-491")
        try {
            item.moveTo(BatchedHoldScheduleStatus.APPROVED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(BatchedHoldScheduleStatus.APPROVED, item.status, "status")
    }

    @Test
    fun `rejects moves from withdrawn`() {
        val item = newBatchedHoldSchedule("code-889", "label-491")
        item.status = BatchedHoldScheduleStatus.WITHDRAWN
        assertFails("move") { item.moveTo(BatchedHoldScheduleStatus.PROPOSED) }
    }
}
