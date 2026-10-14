package com.acme.transport.freightpay.dispute.review

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class DisputeReviewTest {
    @Test
    fun `starts in proposed`() {
        val item = newDisputeReview("code-811", "title-527")
        assertEquals(DisputeReviewStatus.PROPOSED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to rejected`() {
        val item = newDisputeReview("code-811", "title-527")
        try {
            item.moveTo(DisputeReviewStatus.REJECTED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(DisputeReviewStatus.REJECTED, item.status, "status")
    }

    @Test
    fun `rejects moves from withdrawn`() {
        val item = newDisputeReview("code-811", "title-527")
        item.status = DisputeReviewStatus.WITHDRAWN
        assertFails("move") { item.moveTo(DisputeReviewStatus.PROPOSED) }
    }
}
