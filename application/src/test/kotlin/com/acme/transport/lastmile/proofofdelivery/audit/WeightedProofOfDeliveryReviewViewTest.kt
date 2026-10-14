package com.acme.transport.lastmile.proofofdelivery.audit

import kotlin.test.Test
import kotlin.test.assertEquals

class WeightedProofOfDeliveryReviewViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newWeightedProofOfDeliveryReview("code-571", "label-512")
        val view = toWeightedProofOfDeliveryReviewView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toWeightedProofOfDeliveryReviewViews(mutableListOf<WeightedProofOfDeliveryReview>(item, item)).size, "views")
    }
}
