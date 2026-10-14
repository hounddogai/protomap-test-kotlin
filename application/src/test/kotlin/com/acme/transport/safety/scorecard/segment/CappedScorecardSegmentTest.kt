package com.acme.transport.safety.scorecard.segment

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class CappedScorecardSegmentTest {
    @Test
    fun `starts in idle`() {
        val item = newCappedScorecardSegment("code-83", "name-426")
        assertEquals(CappedScorecardSegmentStatus.IDLE, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to ready`() {
        val item = newCappedScorecardSegment("code-83", "name-426")
        try {
            item.moveTo(CappedScorecardSegmentStatus.READY)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(CappedScorecardSegmentStatus.READY, item.status, "status")
    }

    @Test
    fun `rejects moves from stopped`() {
        val item = newCappedScorecardSegment("code-83", "name-426")
        item.status = CappedScorecardSegmentStatus.STOPPED
        assertFails("move") { item.moveTo(CappedScorecardSegmentStatus.IDLE) }
    }
}
