package com.acme.transport.safety.scorecard.proposal

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class ScorecardSegmentTest {
    @Test
    fun `starts in requested`() {
        val item = newScorecardSegment("code-744", "label-179")
        assertEquals(ScorecardSegmentStatus.REQUESTED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to granted`() {
        val item = newScorecardSegment("code-744", "label-179")
        try {
            item.moveTo(ScorecardSegmentStatus.GRANTED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(ScorecardSegmentStatus.GRANTED, item.status, "status")
    }

    @Test
    fun `rejects moves from expired`() {
        val item = newScorecardSegment("code-744", "label-179")
        item.status = ScorecardSegmentStatus.EXPIRED
        assertFails("move") { item.moveTo(ScorecardSegmentStatus.REQUESTED) }
    }
}
