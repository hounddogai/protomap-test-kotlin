package com.acme.transport.safety.coaching.agreement

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class RollingCoachingSegmentTest {
    @Test
    fun `starts in requested`() {
        val item = newRollingCoachingSegment("code-791", "title-802")
        assertEquals(RollingCoachingSegmentStatus.REQUESTED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to expired`() {
        val item = newRollingCoachingSegment("code-791", "title-802")
        try {
            item.moveTo(RollingCoachingSegmentStatus.EXPIRED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(RollingCoachingSegmentStatus.EXPIRED, item.status, "status")
    }

    @Test
    fun `rejects moves from expired`() {
        val item = newRollingCoachingSegment("code-791", "title-802")
        item.status = RollingCoachingSegmentStatus.EXPIRED
        assertFails("move") { item.moveTo(RollingCoachingSegmentStatus.REQUESTED) }
    }
}
