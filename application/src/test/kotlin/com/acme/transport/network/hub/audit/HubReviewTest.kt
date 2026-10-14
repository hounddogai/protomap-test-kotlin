package com.acme.transport.network.hub.audit

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class HubReviewTest {
    @Test
    fun `starts in draft`() {
        val item = newHubReview("code-261", "title-27")
        assertEquals(HubReviewStatus.DRAFT, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to pending`() {
        val item = newHubReview("code-261", "title-27")
        try {
            item.moveTo(HubReviewStatus.PENDING)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(HubReviewStatus.PENDING, item.status, "status")
    }

    @Test
    fun `rejects moves from closed`() {
        val item = newHubReview("code-261", "title-27")
        item.status = HubReviewStatus.CLOSED
        assertFails("move") { item.moveTo(HubReviewStatus.DRAFT) }
    }
}
