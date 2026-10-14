package com.acme.transport.crewing.roster.budget

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class RosterSegmentTest {
    @Test
    fun `starts in open`() {
        val item = newRosterSegment("code-22", "label-111")
        assertEquals(RosterSegmentStatus.OPEN, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to reopened`() {
        val item = newRosterSegment("code-22", "label-111")
        try {
            item.moveTo(RosterSegmentStatus.REOPENED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(RosterSegmentStatus.REOPENED, item.status, "status")
    }

    @Test
    fun `rejects moves from archived`() {
        val item = newRosterSegment("code-22", "label-111")
        item.status = RosterSegmentStatus.ARCHIVED
        assertFails("move") { item.moveTo(RosterSegmentStatus.OPEN) }
    }
}
