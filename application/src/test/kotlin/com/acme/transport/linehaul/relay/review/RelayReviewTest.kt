package com.acme.transport.linehaul.relay.review

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class RelayReviewTest {
    @Test
    fun `starts in open`() {
        val item = newRelayReview("code-879", "name-520")
        assertEquals(RelayReviewStatus.OPEN, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to resolved`() {
        val item = newRelayReview("code-879", "name-520")
        try {
            item.moveTo(RelayReviewStatus.RESOLVED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(RelayReviewStatus.RESOLVED, item.status, "status")
    }

    @Test
    fun `rejects moves from archived`() {
        val item = newRelayReview("code-879", "name-520")
        item.status = RelayReviewStatus.ARCHIVED
        assertFails("move") { item.moveTo(RelayReviewStatus.OPEN) }
    }
}
