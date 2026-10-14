package com.acme.transport.terminal.sortation.bundle

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class SortationSegmentTest {
    @Test
    fun `starts in open`() {
        val item = newSortationSegment("code-982", "label-107")
        assertEquals(SortationSegmentStatus.OPEN, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to in review`() {
        val item = newSortationSegment("code-982", "label-107")
        try {
            item.moveTo(SortationSegmentStatus.IN_REVIEW)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(SortationSegmentStatus.IN_REVIEW, item.status, "status")
    }

    @Test
    fun `rejects moves from archived`() {
        val item = newSortationSegment("code-982", "label-107")
        item.status = SortationSegmentStatus.ARCHIVED
        assertFails("move") { item.moveTo(SortationSegmentStatus.OPEN) }
    }
}
