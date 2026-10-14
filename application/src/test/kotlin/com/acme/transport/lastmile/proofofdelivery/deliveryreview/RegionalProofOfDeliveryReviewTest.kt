package com.acme.transport.lastmile.proofofdelivery.deliveryreview

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class RegionalProofOfDeliveryReviewTest {
    @Test
    fun `starts in new`() {
        val item = newRegionalProofOfDeliveryReview("code-97", "name-763")
        assertEquals(RegionalProofOfDeliveryReviewStatus.NEW, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to done`() {
        val item = newRegionalProofOfDeliveryReview("code-97", "name-763")
        try {
            item.moveTo(RegionalProofOfDeliveryReviewStatus.DONE)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(RegionalProofOfDeliveryReviewStatus.DONE, item.status, "status")
    }

    @Test
    fun `rejects moves from failed`() {
        val item = newRegionalProofOfDeliveryReview("code-97", "name-763")
        item.status = RegionalProofOfDeliveryReviewStatus.FAILED
        assertFails("move") { item.moveTo(RegionalProofOfDeliveryReviewStatus.NEW) }
    }
}
