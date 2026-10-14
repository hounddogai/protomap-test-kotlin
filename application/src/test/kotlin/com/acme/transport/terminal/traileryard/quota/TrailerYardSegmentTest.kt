package com.acme.transport.terminal.traileryard.quota

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class TrailerYardSegmentTest {
    @Test
    fun `starts in open`() {
        val item = newTrailerYardSegment("code-775", "title-471")
        assertEquals(TrailerYardSegmentStatus.OPEN, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to in review`() {
        val item = newTrailerYardSegment("code-775", "title-471")
        try {
            item.moveTo(TrailerYardSegmentStatus.IN_REVIEW)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(TrailerYardSegmentStatus.IN_REVIEW, item.status, "status")
    }

    @Test
    fun `rejects moves from archived`() {
        val item = newTrailerYardSegment("code-775", "title-471")
        item.status = TrailerYardSegmentStatus.ARCHIVED
        assertFails("move") { item.moveTo(TrailerYardSegmentStatus.OPEN) }
    }
}
