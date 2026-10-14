package com.acme.transport.lastmile.proofofdelivery.deliveryallocation

import kotlin.test.Test
import kotlin.test.assertEquals

class SeasonalProofOfDeliveryAllocationViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newSeasonalProofOfDeliveryAllocation("code-6", "label-31")
        val view = toSeasonalProofOfDeliveryAllocationView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toSeasonalProofOfDeliveryAllocationViews(mutableListOf<SeasonalProofOfDeliveryAllocation>(item, item)).size, "views")
    }
}
