package com.acme.transport.lastmile.proofofdelivery.deliveryreview

import kotlin.test.Test
import kotlin.test.assertEquals

class RegionalProofOfDeliveryReviewViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newRegionalProofOfDeliveryReview("code-97", "name-763")
        val view = toRegionalProofOfDeliveryReviewView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toRegionalProofOfDeliveryReviewViews(mutableListOf<RegionalProofOfDeliveryReview>(item, item)).size, "views")
    }
}
