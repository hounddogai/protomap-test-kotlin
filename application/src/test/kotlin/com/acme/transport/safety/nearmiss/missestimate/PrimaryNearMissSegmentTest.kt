package com.acme.transport.safety.nearmiss.missestimate

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class PrimaryNearMissSegmentTest {
    @Test
    fun `starts in planned`() {
        val item = newPrimaryNearMissSegment("code-870", "title-413")
        assertEquals(PrimaryNearMissSegmentStatus.PLANNED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to scheduled`() {
        val item = newPrimaryNearMissSegment("code-870", "title-413")
        try {
            item.moveTo(PrimaryNearMissSegmentStatus.SCHEDULED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(PrimaryNearMissSegmentStatus.SCHEDULED, item.status, "status")
    }

    @Test
    fun `rejects moves from cancelled`() {
        val item = newPrimaryNearMissSegment("code-870", "title-413")
        item.status = PrimaryNearMissSegmentStatus.CANCELLED
        assertFails("move") { item.moveTo(PrimaryNearMissSegmentStatus.PLANNED) }
    }
}
