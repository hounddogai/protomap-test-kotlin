package com.acme.transport.safety.scorecard.segment

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class StrictScorecardSegmentTest {
    @Test
    fun `starts in proposed`() {
        val item = newStrictScorecardSegment("code-222", "label-905")
        assertEquals(StrictScorecardSegmentStatus.PROPOSED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to withdrawn`() {
        val item = newStrictScorecardSegment("code-222", "label-905")
        try {
            item.moveTo(StrictScorecardSegmentStatus.WITHDRAWN)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(StrictScorecardSegmentStatus.WITHDRAWN, item.status, "status")
    }

    @Test
    fun `rejects moves from withdrawn`() {
        val item = newStrictScorecardSegment("code-222", "label-905")
        item.status = StrictScorecardSegmentStatus.WITHDRAWN
        assertFails("move") { item.moveTo(StrictScorecardSegmentStatus.PROPOSED) }
    }
}
