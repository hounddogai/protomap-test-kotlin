package com.acme.transport.lastmile.proofofdelivery.cycle

import kotlin.test.Test
import kotlin.test.assertEquals

class BatchedProofOfDeliveryReviewViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newBatchedProofOfDeliveryReview("code-875", "name-226")
        val view = toBatchedProofOfDeliveryReviewView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toBatchedProofOfDeliveryReviewViews(mutableListOf<BatchedProofOfDeliveryReview>(item, item)).size, "views")
    }
}
