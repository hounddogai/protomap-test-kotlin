package com.acme.transport.emissions.routeoffset.segment

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class LenientRouteOffsetSegmentTest {
    @Test
    fun `starts in idle`() {
        val item = newLenientRouteOffsetSegment("code-184", "name-654")
        assertEquals(LenientRouteOffsetSegmentStatus.IDLE, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to stopped`() {
        val item = newLenientRouteOffsetSegment("code-184", "name-654")
        try {
            item.moveTo(LenientRouteOffsetSegmentStatus.STOPPED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(LenientRouteOffsetSegmentStatus.STOPPED, item.status, "status")
    }

    @Test
    fun `rejects moves from stopped`() {
        val item = newLenientRouteOffsetSegment("code-184", "name-654")
        item.status = LenientRouteOffsetSegmentStatus.STOPPED
        assertFails("move") { item.moveTo(LenientRouteOffsetSegmentStatus.IDLE) }
    }
}
