package com.acme.transport.crewing.qualification.tieredqualificationdigest

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class BatchedQualificationCheckpointTest {
    @Test
    fun `starts in proposed`() {
        val item = newBatchedQualificationCheckpoint("code-605", "title-187")
        assertEquals(BatchedQualificationCheckpointStatus.PROPOSED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to approved`() {
        val item = newBatchedQualificationCheckpoint("code-605", "title-187")
        try {
            item.moveTo(BatchedQualificationCheckpointStatus.APPROVED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(BatchedQualificationCheckpointStatus.APPROVED, item.status, "status")
    }

    @Test
    fun `rejects moves from withdrawn`() {
        val item = newBatchedQualificationCheckpoint("code-605", "title-187")
        item.status = BatchedQualificationCheckpointStatus.WITHDRAWN
        assertFails("move") { item.moveTo(BatchedQualificationCheckpointStatus.PROPOSED) }
    }
}
