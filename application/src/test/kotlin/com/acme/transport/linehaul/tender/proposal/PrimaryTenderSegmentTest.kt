package com.acme.transport.linehaul.tender.proposal

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class PrimaryTenderSegmentTest {
    @Test
    fun `starts in proposed`() {
        val item = newPrimaryTenderSegment("code-477", "title-723")
        assertEquals(PrimaryTenderSegmentStatus.PROPOSED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to approved`() {
        val item = newPrimaryTenderSegment("code-477", "title-723")
        try {
            item.moveTo(PrimaryTenderSegmentStatus.APPROVED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(PrimaryTenderSegmentStatus.APPROVED, item.status, "status")
    }

    @Test
    fun `rejects moves from withdrawn`() {
        val item = newPrimaryTenderSegment("code-477", "title-723")
        item.status = PrimaryTenderSegmentStatus.WITHDRAWN
        assertFails("move") { item.moveTo(PrimaryTenderSegmentStatus.PROPOSED) }
    }
}
