package com.acme.transport.safety.coaching.score

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class BatchedCoachingDigestTest {
    @Test
    fun `starts in proposed`() {
        val item = newBatchedCoachingDigest("code-280", "name-628")
        assertEquals(BatchedCoachingDigestStatus.PROPOSED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to approved`() {
        val item = newBatchedCoachingDigest("code-280", "name-628")
        try {
            item.moveTo(BatchedCoachingDigestStatus.APPROVED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(BatchedCoachingDigestStatus.APPROVED, item.status, "status")
    }

    @Test
    fun `rejects moves from withdrawn`() {
        val item = newBatchedCoachingDigest("code-280", "name-628")
        item.status = BatchedCoachingDigestStatus.WITHDRAWN
        assertFails("move") { item.moveTo(BatchedCoachingDigestStatus.PROPOSED) }
    }
}
