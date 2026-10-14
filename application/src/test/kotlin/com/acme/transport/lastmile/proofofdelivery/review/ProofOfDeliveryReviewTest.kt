package com.acme.transport.lastmile.proofofdelivery.review

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class ProofOfDeliveryReviewTest {
    @Test
    fun `starts in proposed`() {
        val item = newProofOfDeliveryReview("code-537", "name-890")
        assertEquals(ProofOfDeliveryReviewStatus.PROPOSED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to approved`() {
        val item = newProofOfDeliveryReview("code-537", "name-890")
        try {
            item.moveTo(ProofOfDeliveryReviewStatus.APPROVED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(ProofOfDeliveryReviewStatus.APPROVED, item.status, "status")
    }

    @Test
    fun `rejects moves from withdrawn`() {
        val item = newProofOfDeliveryReview("code-537", "name-890")
        item.status = ProofOfDeliveryReviewStatus.WITHDRAWN
        assertFails("move") { item.moveTo(ProofOfDeliveryReviewStatus.PROPOSED) }
    }
}
