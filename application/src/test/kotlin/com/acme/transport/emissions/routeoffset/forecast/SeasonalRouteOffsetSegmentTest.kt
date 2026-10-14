package com.acme.transport.emissions.routeoffset.forecast

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class SeasonalRouteOffsetSegmentTest {
    @Test
    fun `starts in requested`() {
        val item = newSeasonalRouteOffsetSegment("code-157", "title-775")
        assertEquals(SeasonalRouteOffsetSegmentStatus.REQUESTED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to expired`() {
        val item = newSeasonalRouteOffsetSegment("code-157", "title-775")
        try {
            item.moveTo(SeasonalRouteOffsetSegmentStatus.EXPIRED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(SeasonalRouteOffsetSegmentStatus.EXPIRED, item.status, "status")
    }

    @Test
    fun `rejects moves from expired`() {
        val item = newSeasonalRouteOffsetSegment("code-157", "title-775")
        item.status = SeasonalRouteOffsetSegmentStatus.EXPIRED
        assertFails("move") { item.moveTo(SeasonalRouteOffsetSegmentStatus.REQUESTED) }
    }
}
