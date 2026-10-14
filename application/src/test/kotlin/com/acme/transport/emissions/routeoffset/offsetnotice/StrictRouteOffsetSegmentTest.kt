package com.acme.transport.emissions.routeoffset.offsetnotice

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class StrictRouteOffsetSegmentTest {
    @Test
    fun `starts in draft`() {
        val item = newStrictRouteOffsetSegment("code-673", "label-60")
        assertEquals(StrictRouteOffsetSegmentStatus.DRAFT, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to pending`() {
        val item = newStrictRouteOffsetSegment("code-673", "label-60")
        try {
            item.moveTo(StrictRouteOffsetSegmentStatus.PENDING)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(StrictRouteOffsetSegmentStatus.PENDING, item.status, "status")
    }

    @Test
    fun `rejects moves from closed`() {
        val item = newStrictRouteOffsetSegment("code-673", "label-60")
        item.status = StrictRouteOffsetSegmentStatus.CLOSED
        assertFails("move") { item.moveTo(StrictRouteOffsetSegmentStatus.DRAFT) }
    }
}
