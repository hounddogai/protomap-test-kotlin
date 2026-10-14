package com.acme.transport.lastmile.attempt.attemptdigest

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class WeightedAttemptDigestTest {
    @Test
    fun `starts in new`() {
        val item = newWeightedAttemptDigest("code-205", "title-390")
        assertEquals(WeightedAttemptDigestStatus.NEW, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to queued`() {
        val item = newWeightedAttemptDigest("code-205", "title-390")
        try {
            item.moveTo(WeightedAttemptDigestStatus.QUEUED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(WeightedAttemptDigestStatus.QUEUED, item.status, "status")
    }

    @Test
    fun `rejects moves from failed`() {
        val item = newWeightedAttemptDigest("code-205", "title-390")
        item.status = WeightedAttemptDigestStatus.FAILED
        assertFails("move") { item.moveTo(WeightedAttemptDigestStatus.NEW) }
    }
}
