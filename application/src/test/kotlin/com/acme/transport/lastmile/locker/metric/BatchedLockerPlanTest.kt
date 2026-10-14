package com.acme.transport.lastmile.locker.metric

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class BatchedLockerPlanTest {
    @Test
    fun `starts in new`() {
        val item = newBatchedLockerPlan("code-954", "label-870")
        assertEquals(BatchedLockerPlanStatus.NEW, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to queued`() {
        val item = newBatchedLockerPlan("code-954", "label-870")
        try {
            item.moveTo(BatchedLockerPlanStatus.QUEUED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(BatchedLockerPlanStatus.QUEUED, item.status, "status")
    }

    @Test
    fun `rejects moves from failed`() {
        val item = newBatchedLockerPlan("code-954", "label-870")
        item.status = BatchedLockerPlanStatus.FAILED
        assertFails("move") { item.moveTo(BatchedLockerPlanStatus.NEW) }
    }
}
