package com.acme.transport.linehaul.trailer.rollup

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class ManualTrailerSegmentTest {
    @Test
    fun `starts in proposed`() {
        val item = newManualTrailerSegment("code-757", "label-518")
        assertEquals(ManualTrailerSegmentStatus.PROPOSED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to approved`() {
        val item = newManualTrailerSegment("code-757", "label-518")
        try {
            item.moveTo(ManualTrailerSegmentStatus.APPROVED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(ManualTrailerSegmentStatus.APPROVED, item.status, "status")
    }

    @Test
    fun `rejects moves from withdrawn`() {
        val item = newManualTrailerSegment("code-757", "label-518")
        item.status = ManualTrailerSegmentStatus.WITHDRAWN
        assertFails("move") { item.moveTo(ManualTrailerSegmentStatus.PROPOSED) }
    }
}
