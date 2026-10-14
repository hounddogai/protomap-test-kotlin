package com.acme.transport.safety.nearmiss.missvariance

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class ScheduledNearMissSegmentTest {
    @Test
    fun `starts in open`() {
        val item = newScheduledNearMissSegment("code-780", "name-504")
        assertEquals(ScheduledNearMissSegmentStatus.OPEN, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to archived`() {
        val item = newScheduledNearMissSegment("code-780", "name-504")
        try {
            item.moveTo(ScheduledNearMissSegmentStatus.ARCHIVED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(ScheduledNearMissSegmentStatus.ARCHIVED, item.status, "status")
    }

    @Test
    fun `rejects moves from archived`() {
        val item = newScheduledNearMissSegment("code-780", "name-504")
        item.status = ScheduledNearMissSegmentStatus.ARCHIVED
        assertFails("move") { item.moveTo(ScheduledNearMissSegmentStatus.OPEN) }
    }
}
