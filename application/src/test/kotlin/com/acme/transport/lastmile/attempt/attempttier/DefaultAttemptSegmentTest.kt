package com.acme.transport.lastmile.attempt.attempttier

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class DefaultAttemptSegmentTest {
    @Test
    fun `starts in requested`() {
        val item = newDefaultAttemptSegment("code-92", "label-758")
        assertEquals(DefaultAttemptSegmentStatus.REQUESTED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to revoked`() {
        val item = newDefaultAttemptSegment("code-92", "label-758")
        try {
            item.moveTo(DefaultAttemptSegmentStatus.REVOKED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(DefaultAttemptSegmentStatus.REVOKED, item.status, "status")
    }

    @Test
    fun `rejects moves from expired`() {
        val item = newDefaultAttemptSegment("code-92", "label-758")
        item.status = DefaultAttemptSegmentStatus.EXPIRED
        assertFails("move") { item.moveTo(DefaultAttemptSegmentStatus.REQUESTED) }
    }
}
