package com.acme.transport.lastmile.proofofdelivery.cycle

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class BatchedProofOfDeliveryReviewTest {
    @Test
    fun `starts in proposed`() {
        val item = newBatchedProofOfDeliveryReview("code-875", "name-226")
        assertEquals(BatchedProofOfDeliveryReviewStatus.PROPOSED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to approved`() {
        val item = newBatchedProofOfDeliveryReview("code-875", "name-226")
        try {
            item.moveTo(BatchedProofOfDeliveryReviewStatus.APPROVED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(BatchedProofOfDeliveryReviewStatus.APPROVED, item.status, "status")
    }

    @Test
    fun `rejects moves from withdrawn`() {
        val item = newBatchedProofOfDeliveryReview("code-875", "name-226")
        item.status = BatchedProofOfDeliveryReviewStatus.WITHDRAWN
        assertFails("move") { item.moveTo(BatchedProofOfDeliveryReviewStatus.PROPOSED) }
    }
}
