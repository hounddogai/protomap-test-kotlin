package com.acme.transport.lastmile.attempt.batch

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class AttemptBatchTest {
    @Test
    fun `starts in open`() {
        val item = newAttemptBatch("code-751", "name-215")
        assertEquals(AttemptBatchStatus.OPEN, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to in review`() {
        val item = newAttemptBatch("code-751", "name-215")
        try {
            item.moveTo(AttemptBatchStatus.IN_REVIEW)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(AttemptBatchStatus.IN_REVIEW, item.status, "status")
    }

    @Test
    fun `rejects moves from archived`() {
        val item = newAttemptBatch("code-751", "name-215")
        item.status = AttemptBatchStatus.ARCHIVED
        assertFails("move") { item.moveTo(AttemptBatchStatus.OPEN) }
    }
}
