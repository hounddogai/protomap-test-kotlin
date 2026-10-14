package com.acme.transport.linehaul.trailer.threshold

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class TrailerSegmentTest {
    @Test
    fun `starts in idle`() {
        val item = newTrailerSegment("code-348", "title-677")
        assertEquals(TrailerSegmentStatus.IDLE, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to ready`() {
        val item = newTrailerSegment("code-348", "title-677")
        try {
            item.moveTo(TrailerSegmentStatus.READY)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(TrailerSegmentStatus.READY, item.status, "status")
    }

    @Test
    fun `rejects moves from stopped`() {
        val item = newTrailerSegment("code-348", "title-677")
        item.status = TrailerSegmentStatus.STOPPED
        assertFails("move") { item.moveTo(TrailerSegmentStatus.IDLE) }
    }
}
