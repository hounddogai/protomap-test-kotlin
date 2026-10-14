package com.acme.transport.crewing.roster.rostermilestone

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class StrictRosterSegmentTest {
    @Test
    fun `starts in new`() {
        val item = newStrictRosterSegment("code-252", "label-555")
        assertEquals(StrictRosterSegmentStatus.NEW, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to queued`() {
        val item = newStrictRosterSegment("code-252", "label-555")
        try {
            item.moveTo(StrictRosterSegmentStatus.QUEUED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(StrictRosterSegmentStatus.QUEUED, item.status, "status")
    }

    @Test
    fun `rejects moves from failed`() {
        val item = newStrictRosterSegment("code-252", "label-555")
        item.status = StrictRosterSegmentStatus.FAILED
        assertFails("move") { item.moveTo(StrictRosterSegmentStatus.NEW) }
    }
}
