package com.acme.transport.lastmile.attempt.segment

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class AttemptSegmentTest {
    @Test
    fun `starts in proposed`() {
        val item = newAttemptSegment("code-8", "name-115")
        assertEquals(AttemptSegmentStatus.PROPOSED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to approved`() {
        val item = newAttemptSegment("code-8", "name-115")
        try {
            item.moveTo(AttemptSegmentStatus.APPROVED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(AttemptSegmentStatus.APPROVED, item.status, "status")
    }

    @Test
    fun `rejects moves from withdrawn`() {
        val item = newAttemptSegment("code-8", "name-115")
        item.status = AttemptSegmentStatus.WITHDRAWN
        assertFails("move") { item.moveTo(AttemptSegmentStatus.PROPOSED) }
    }
}
