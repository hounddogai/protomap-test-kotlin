package com.acme.transport.crewing.roster.segment

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class RegionalRosterSegmentTest {
    @Test
    fun `starts in draft`() {
        val item = newRegionalRosterSegment("code-184", "name-594")
        assertEquals(RegionalRosterSegmentStatus.DRAFT, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to active`() {
        val item = newRegionalRosterSegment("code-184", "name-594")
        try {
            item.moveTo(RegionalRosterSegmentStatus.ACTIVE)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(RegionalRosterSegmentStatus.ACTIVE, item.status, "status")
    }

    @Test
    fun `rejects moves from closed`() {
        val item = newRegionalRosterSegment("code-184", "name-594")
        item.status = RegionalRosterSegmentStatus.CLOSED
        assertFails("move") { item.moveTo(RegionalRosterSegmentStatus.DRAFT) }
    }
}
