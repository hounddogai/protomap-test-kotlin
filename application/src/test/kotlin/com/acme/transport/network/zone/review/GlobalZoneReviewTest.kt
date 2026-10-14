package com.acme.transport.network.zone.review

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class GlobalZoneReviewTest {
    @Test
    fun `starts in proposed`() {
        val item = newGlobalZoneReview("code-846", "title-116")
        assertEquals(GlobalZoneReviewStatus.PROPOSED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to approved`() {
        val item = newGlobalZoneReview("code-846", "title-116")
        try {
            item.moveTo(GlobalZoneReviewStatus.APPROVED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(GlobalZoneReviewStatus.APPROVED, item.status, "status")
    }

    @Test
    fun `rejects moves from withdrawn`() {
        val item = newGlobalZoneReview("code-846", "title-116")
        item.status = GlobalZoneReviewStatus.WITHDRAWN
        assertFails("move") { item.moveTo(GlobalZoneReviewStatus.PROPOSED) }
    }
}
