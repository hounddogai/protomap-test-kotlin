package com.acme.transport.lastmile.attempt.window

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class CappedAttemptSegmentTest {
    @Test
    fun `starts in idle`() {
        val item = newCappedAttemptSegment("code-245", "title-852")
        assertEquals(CappedAttemptSegmentStatus.IDLE, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to ready`() {
        val item = newCappedAttemptSegment("code-245", "title-852")
        try {
            item.moveTo(CappedAttemptSegmentStatus.READY)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(CappedAttemptSegmentStatus.READY, item.status, "status")
    }

    @Test
    fun `rejects moves from stopped`() {
        val item = newCappedAttemptSegment("code-245", "title-852")
        item.status = CappedAttemptSegmentStatus.STOPPED
        assertFails("move") { item.moveTo(CappedAttemptSegmentStatus.IDLE) }
    }
}
