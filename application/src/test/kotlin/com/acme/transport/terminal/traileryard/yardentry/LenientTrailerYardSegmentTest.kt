package com.acme.transport.terminal.traileryard.yardentry

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class LenientTrailerYardSegmentTest {
    @Test
    fun `starts in new`() {
        val item = newLenientTrailerYardSegment("code-492", "label-772")
        assertEquals(LenientTrailerYardSegmentStatus.NEW, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to done`() {
        val item = newLenientTrailerYardSegment("code-492", "label-772")
        try {
            item.moveTo(LenientTrailerYardSegmentStatus.DONE)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(LenientTrailerYardSegmentStatus.DONE, item.status, "status")
    }

    @Test
    fun `rejects moves from failed`() {
        val item = newLenientTrailerYardSegment("code-492", "label-772")
        item.status = LenientTrailerYardSegmentStatus.FAILED
        assertFails("move") { item.moveTo(LenientTrailerYardSegmentStatus.NEW) }
    }
}
