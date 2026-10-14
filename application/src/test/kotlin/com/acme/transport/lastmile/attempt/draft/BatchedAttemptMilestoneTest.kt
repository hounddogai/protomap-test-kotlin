package com.acme.transport.lastmile.attempt.draft

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class BatchedAttemptMilestoneTest {
    @Test
    fun `starts in new`() {
        val item = newBatchedAttemptMilestone("code-344", "name-434")
        assertEquals(BatchedAttemptMilestoneStatus.NEW, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to running`() {
        val item = newBatchedAttemptMilestone("code-344", "name-434")
        try {
            item.moveTo(BatchedAttemptMilestoneStatus.RUNNING)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(BatchedAttemptMilestoneStatus.RUNNING, item.status, "status")
    }

    @Test
    fun `rejects moves from failed`() {
        val item = newBatchedAttemptMilestone("code-344", "name-434")
        item.status = BatchedAttemptMilestoneStatus.FAILED
        assertFails("move") { item.moveTo(BatchedAttemptMilestoneStatus.NEW) }
    }
}
