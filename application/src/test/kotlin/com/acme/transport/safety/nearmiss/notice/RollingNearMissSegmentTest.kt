package com.acme.transport.safety.nearmiss.notice

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class RollingNearMissSegmentTest {
    @Test
    fun `starts in proposed`() {
        val item = newRollingNearMissSegment("code-791", "label-47")
        assertEquals(RollingNearMissSegmentStatus.PROPOSED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to rejected`() {
        val item = newRollingNearMissSegment("code-791", "label-47")
        try {
            item.moveTo(RollingNearMissSegmentStatus.REJECTED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(RollingNearMissSegmentStatus.REJECTED, item.status, "status")
    }

    @Test
    fun `rejects moves from withdrawn`() {
        val item = newRollingNearMissSegment("code-791", "label-47")
        item.status = RollingNearMissSegmentStatus.WITHDRAWN
        assertFails("move") { item.moveTo(RollingNearMissSegmentStatus.PROPOSED) }
    }
}
