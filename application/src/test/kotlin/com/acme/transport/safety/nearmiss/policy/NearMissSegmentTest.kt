package com.acme.transport.safety.nearmiss.policy

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class NearMissSegmentTest {
    @Test
    fun `starts in open`() {
        val item = newNearMissSegment("code-101", "label-889")
        assertEquals(NearMissSegmentStatus.OPEN, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to in review`() {
        val item = newNearMissSegment("code-101", "label-889")
        try {
            item.moveTo(NearMissSegmentStatus.IN_REVIEW)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(NearMissSegmentStatus.IN_REVIEW, item.status, "status")
    }

    @Test
    fun `rejects moves from archived`() {
        val item = newNearMissSegment("code-101", "label-889")
        item.status = NearMissSegmentStatus.ARCHIVED
        assertFails("move") { item.moveTo(NearMissSegmentStatus.OPEN) }
    }
}
