package com.acme.transport.safety.scorecard.scorecardgroup

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class AdaptiveScorecardSegmentTest {
    @Test
    fun `starts in draft`() {
        val item = newAdaptiveScorecardSegment("code-904", "title-718")
        assertEquals(AdaptiveScorecardSegmentStatus.DRAFT, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to suspended`() {
        val item = newAdaptiveScorecardSegment("code-904", "title-718")
        try {
            item.moveTo(AdaptiveScorecardSegmentStatus.SUSPENDED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(AdaptiveScorecardSegmentStatus.SUSPENDED, item.status, "status")
    }

    @Test
    fun `rejects moves from closed`() {
        val item = newAdaptiveScorecardSegment("code-904", "title-718")
        item.status = AdaptiveScorecardSegmentStatus.CLOSED
        assertFails("move") { item.moveTo(AdaptiveScorecardSegmentStatus.DRAFT) }
    }
}
