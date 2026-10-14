package com.acme.transport.lastmile.proofofdelivery.audit

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class WeightedProofOfDeliveryReviewTest {
    @Test
    fun `starts in draft`() {
        val item = newWeightedProofOfDeliveryReview("code-571", "label-512")
        assertEquals(WeightedProofOfDeliveryReviewStatus.DRAFT, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to pending`() {
        val item = newWeightedProofOfDeliveryReview("code-571", "label-512")
        try {
            item.moveTo(WeightedProofOfDeliveryReviewStatus.PENDING)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(WeightedProofOfDeliveryReviewStatus.PENDING, item.status, "status")
    }

    @Test
    fun `rejects moves from closed`() {
        val item = newWeightedProofOfDeliveryReview("code-571", "label-512")
        item.status = WeightedProofOfDeliveryReviewStatus.CLOSED
        assertFails("move") { item.moveTo(WeightedProofOfDeliveryReviewStatus.DRAFT) }
    }
}
