package com.acme.transport.linehaul.trailer.segment

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class PendingTrailerSegmentTest {
    @Test
    fun `starts in created`() {
        val item = newPendingTrailerSegment("code-670", "label-905")
        assertEquals(PendingTrailerSegmentStatus.CREATED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to published`() {
        val item = newPendingTrailerSegment("code-670", "label-905")
        try {
            item.moveTo(PendingTrailerSegmentStatus.PUBLISHED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(PendingTrailerSegmentStatus.PUBLISHED, item.status, "status")
    }

    @Test
    fun `rejects moves from retired`() {
        val item = newPendingTrailerSegment("code-670", "label-905")
        item.status = PendingTrailerSegmentStatus.RETIRED
        assertFails("move") { item.moveTo(PendingTrailerSegmentStatus.CREATED) }
    }
}
