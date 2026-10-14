package com.acme.transport.terminal.traileryard.tier

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class BatchedTrailerYardAuditTest {
    @Test
    fun `starts in draft`() {
        val item = newBatchedTrailerYardAudit("code-428", "label-294")
        assertEquals(BatchedTrailerYardAuditStatus.DRAFT, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to pending`() {
        val item = newBatchedTrailerYardAudit("code-428", "label-294")
        try {
            item.moveTo(BatchedTrailerYardAuditStatus.PENDING)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(BatchedTrailerYardAuditStatus.PENDING, item.status, "status")
    }

    @Test
    fun `rejects moves from closed`() {
        val item = newBatchedTrailerYardAudit("code-428", "label-294")
        item.status = BatchedTrailerYardAuditStatus.CLOSED
        assertFails("move") { item.moveTo(BatchedTrailerYardAuditStatus.DRAFT) }
    }
}
