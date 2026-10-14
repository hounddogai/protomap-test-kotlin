package com.acme.transport.lastmile.proofofdelivery.deliveryrecord

import kotlin.test.Test
import kotlin.test.assertEquals

class WeightedProofOfDeliveryOverrideViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newWeightedProofOfDeliveryOverride("code-621", "title-365")
        val view = toWeightedProofOfDeliveryOverrideView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toWeightedProofOfDeliveryOverrideViews(mutableListOf<WeightedProofOfDeliveryOverride>(item, item)).size, "views")
    }
}
