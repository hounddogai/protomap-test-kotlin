package com.acme.transport.safety.nearmiss.segment

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class AdaptiveNearMissSegmentTest {
    @Test
    fun `starts in planned`() {
        val item = newAdaptiveNearMissSegment("code-763", "title-765")
        assertEquals(AdaptiveNearMissSegmentStatus.PLANNED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to cancelled`() {
        val item = newAdaptiveNearMissSegment("code-763", "title-765")
        try {
            item.moveTo(AdaptiveNearMissSegmentStatus.CANCELLED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(AdaptiveNearMissSegmentStatus.CANCELLED, item.status, "status")
    }

    @Test
    fun `rejects moves from cancelled`() {
        val item = newAdaptiveNearMissSegment("code-763", "title-765")
        item.status = AdaptiveNearMissSegmentStatus.CANCELLED
        assertFails("move") { item.moveTo(AdaptiveNearMissSegmentStatus.PLANNED) }
    }
}
