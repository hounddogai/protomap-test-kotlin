package com.acme.transport.safety.coaching.coachingsegment

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class GlobalCoachingSegmentTest {
    @Test
    fun `starts in planned`() {
        val item = newGlobalCoachingSegment("code-871", "name-947")
        assertEquals(GlobalCoachingSegmentStatus.PLANNED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to scheduled`() {
        val item = newGlobalCoachingSegment("code-871", "name-947")
        try {
            item.moveTo(GlobalCoachingSegmentStatus.SCHEDULED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(GlobalCoachingSegmentStatus.SCHEDULED, item.status, "status")
    }

    @Test
    fun `rejects moves from cancelled`() {
        val item = newGlobalCoachingSegment("code-871", "name-947")
        item.status = GlobalCoachingSegmentStatus.CANCELLED
        assertFails("move") { item.moveTo(GlobalCoachingSegmentStatus.PLANNED) }
    }
}
