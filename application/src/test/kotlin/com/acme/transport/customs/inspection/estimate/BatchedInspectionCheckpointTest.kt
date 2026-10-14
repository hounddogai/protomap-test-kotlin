package com.acme.transport.customs.inspection.estimate

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class BatchedInspectionCheckpointTest {
    @Test
    fun `starts in proposed`() {
        val item = newBatchedInspectionCheckpoint("code-328", "title-307")
        assertEquals(BatchedInspectionCheckpointStatus.PROPOSED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to rejected`() {
        val item = newBatchedInspectionCheckpoint("code-328", "title-307")
        try {
            item.moveTo(BatchedInspectionCheckpointStatus.REJECTED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(BatchedInspectionCheckpointStatus.REJECTED, item.status, "status")
    }

    @Test
    fun `rejects moves from withdrawn`() {
        val item = newBatchedInspectionCheckpoint("code-328", "title-307")
        item.status = BatchedInspectionCheckpointStatus.WITHDRAWN
        assertFails("move") { item.moveTo(BatchedInspectionCheckpointStatus.PROPOSED) }
    }
}
