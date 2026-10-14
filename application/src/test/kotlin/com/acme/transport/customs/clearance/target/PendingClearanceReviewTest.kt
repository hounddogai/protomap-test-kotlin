package com.acme.transport.customs.clearance.target

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class PendingClearanceReviewTest {
    @Test
    fun `starts in open`() {
        val item = newPendingClearanceReview("code-814", "title-473")
        assertEquals(PendingClearanceReviewStatus.OPEN, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to resolved`() {
        val item = newPendingClearanceReview("code-814", "title-473")
        try {
            item.moveTo(PendingClearanceReviewStatus.RESOLVED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(PendingClearanceReviewStatus.RESOLVED, item.status, "status")
    }

    @Test
    fun `rejects moves from archived`() {
        val item = newPendingClearanceReview("code-814", "title-473")
        item.status = PendingClearanceReviewStatus.ARCHIVED
        assertFails("move") { item.moveTo(PendingClearanceReviewStatus.OPEN) }
    }
}
