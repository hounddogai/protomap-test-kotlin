package com.acme.transport.lastmile.redelivery.schedule

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class RedeliveryReviewTest {
    @Test
    fun `starts in proposed`() {
        val item = newRedeliveryReview("code-177", "title-72")
        assertEquals(RedeliveryReviewStatus.PROPOSED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to approved`() {
        val item = newRedeliveryReview("code-177", "title-72")
        try {
            item.moveTo(RedeliveryReviewStatus.APPROVED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(RedeliveryReviewStatus.APPROVED, item.status, "status")
    }

    @Test
    fun `rejects moves from withdrawn`() {
        val item = newRedeliveryReview("code-177", "title-72")
        item.status = RedeliveryReviewStatus.WITHDRAWN
        assertFails("move") { item.moveTo(RedeliveryReviewStatus.PROPOSED) }
    }
}
