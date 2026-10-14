package com.acme.transport.lastmile.locker.adjustment

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class BatchedLockerAdjustmentTest {
    @Test
    fun `starts in draft`() {
        val item = newBatchedLockerAdjustment("code-288", "name-143")
        assertEquals(BatchedLockerAdjustmentStatus.DRAFT, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to pending`() {
        val item = newBatchedLockerAdjustment("code-288", "name-143")
        try {
            item.moveTo(BatchedLockerAdjustmentStatus.PENDING)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(BatchedLockerAdjustmentStatus.PENDING, item.status, "status")
    }

    @Test
    fun `rejects moves from closed`() {
        val item = newBatchedLockerAdjustment("code-288", "name-143")
        item.status = BatchedLockerAdjustmentStatus.CLOSED
        assertFails("move") { item.moveTo(BatchedLockerAdjustmentStatus.DRAFT) }
    }
}
