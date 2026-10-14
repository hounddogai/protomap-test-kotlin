package com.acme.transport.network.transittime.slot

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class PrimaryTransitTimeSegmentTest {
    @Test
    fun `starts in planned`() {
        val item = newPrimaryTransitTimeSegment("code-479", "label-437")
        assertEquals(PrimaryTransitTimeSegmentStatus.PLANNED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to cancelled`() {
        val item = newPrimaryTransitTimeSegment("code-479", "label-437")
        try {
            item.moveTo(PrimaryTransitTimeSegmentStatus.CANCELLED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(PrimaryTransitTimeSegmentStatus.CANCELLED, item.status, "status")
    }

    @Test
    fun `rejects moves from cancelled`() {
        val item = newPrimaryTransitTimeSegment("code-479", "label-437")
        item.status = PrimaryTransitTimeSegmentStatus.CANCELLED
        assertFails("move") { item.moveTo(PrimaryTransitTimeSegmentStatus.PLANNED) }
    }
}
