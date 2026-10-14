package com.acme.transport.safety.violation.batch

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class BatchedViolationDraftTest {
    @Test
    fun `starts in new`() {
        val item = newBatchedViolationDraft("code-9", "label-956")
        assertEquals(BatchedViolationDraftStatus.NEW, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to queued`() {
        val item = newBatchedViolationDraft("code-9", "label-956")
        try {
            item.moveTo(BatchedViolationDraftStatus.QUEUED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(BatchedViolationDraftStatus.QUEUED, item.status, "status")
    }

    @Test
    fun `rejects moves from failed`() {
        val item = newBatchedViolationDraft("code-9", "label-956")
        item.status = BatchedViolationDraftStatus.FAILED
        assertFails("move") { item.moveTo(BatchedViolationDraftStatus.NEW) }
    }
}
