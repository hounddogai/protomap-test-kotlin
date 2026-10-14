package com.acme.transport.lastmile.attempt.attempttier

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class BatchedAttemptTierTest {
    @Test
    fun `starts in proposed`() {
        val item = newBatchedAttemptTier("code-932", "label-271")
        assertEquals(BatchedAttemptTierStatus.PROPOSED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to approved`() {
        val item = newBatchedAttemptTier("code-932", "label-271")
        try {
            item.moveTo(BatchedAttemptTierStatus.APPROVED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(BatchedAttemptTierStatus.APPROVED, item.status, "status")
    }

    @Test
    fun `rejects moves from withdrawn`() {
        val item = newBatchedAttemptTier("code-932", "label-271")
        item.status = BatchedAttemptTierStatus.WITHDRAWN
        assertFails("move") { item.moveTo(BatchedAttemptTierStatus.PROPOSED) }
    }
}
