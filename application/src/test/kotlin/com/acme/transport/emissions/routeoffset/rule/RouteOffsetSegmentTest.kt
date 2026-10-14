package com.acme.transport.emissions.routeoffset.rule

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class RouteOffsetSegmentTest {
    @Test
    fun `starts in requested`() {
        val item = newRouteOffsetSegment("code-454", "name-435")
        assertEquals(RouteOffsetSegmentStatus.REQUESTED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to granted`() {
        val item = newRouteOffsetSegment("code-454", "name-435")
        try {
            item.moveTo(RouteOffsetSegmentStatus.GRANTED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(RouteOffsetSegmentStatus.GRANTED, item.status, "status")
    }

    @Test
    fun `rejects moves from expired`() {
        val item = newRouteOffsetSegment("code-454", "name-435")
        item.status = RouteOffsetSegmentStatus.EXPIRED
        assertFails("move") { item.moveTo(RouteOffsetSegmentStatus.REQUESTED) }
    }
}
