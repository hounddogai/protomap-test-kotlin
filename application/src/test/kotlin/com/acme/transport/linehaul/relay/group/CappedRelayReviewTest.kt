package com.acme.transport.linehaul.relay.group

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class CappedRelayReviewTest {
    @Test
    fun `starts in open`() {
        val item = newCappedRelayReview("code-273", "name-408")
        assertEquals(CappedRelayReviewStatus.OPEN, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to in review`() {
        val item = newCappedRelayReview("code-273", "name-408")
        try {
            item.moveTo(CappedRelayReviewStatus.IN_REVIEW)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(CappedRelayReviewStatus.IN_REVIEW, item.status, "status")
    }

    @Test
    fun `rejects moves from archived`() {
        val item = newCappedRelayReview("code-273", "name-408")
        item.status = CappedRelayReviewStatus.ARCHIVED
        assertFails("move") { item.moveTo(CappedRelayReviewStatus.OPEN) }
    }
}
