package com.acme.transport.crewing.qualification.schedule

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class BatchedQualificationMilestoneTest {
    @Test
    fun `starts in new`() {
        val item = newBatchedQualificationMilestone("code-623", "title-367")
        assertEquals(BatchedQualificationMilestoneStatus.NEW, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to queued`() {
        val item = newBatchedQualificationMilestone("code-623", "title-367")
        try {
            item.moveTo(BatchedQualificationMilestoneStatus.QUEUED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(BatchedQualificationMilestoneStatus.QUEUED, item.status, "status")
    }

    @Test
    fun `rejects moves from failed`() {
        val item = newBatchedQualificationMilestone("code-623", "title-367")
        item.status = BatchedQualificationMilestoneStatus.FAILED
        assertFails("move") { item.moveTo(BatchedQualificationMilestoneStatus.NEW) }
    }
}
