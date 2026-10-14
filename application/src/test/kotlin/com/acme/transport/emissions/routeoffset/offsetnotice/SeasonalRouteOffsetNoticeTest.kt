package com.acme.transport.emissions.routeoffset.offsetnotice

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class SeasonalRouteOffsetNoticeTest {
    @Test
    fun `starts in open`() {
        val item = newSeasonalRouteOffsetNotice("code-462", "name-357")
        assertEquals(SeasonalRouteOffsetNoticeStatus.OPEN, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to in review`() {
        val item = newSeasonalRouteOffsetNotice("code-462", "name-357")
        try {
            item.moveTo(SeasonalRouteOffsetNoticeStatus.IN_REVIEW)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(SeasonalRouteOffsetNoticeStatus.IN_REVIEW, item.status, "status")
    }

    @Test
    fun `rejects moves from archived`() {
        val item = newSeasonalRouteOffsetNotice("code-462", "name-357")
        item.status = SeasonalRouteOffsetNoticeStatus.ARCHIVED
        assertFails("move") { item.moveTo(SeasonalRouteOffsetNoticeStatus.OPEN) }
    }
}
