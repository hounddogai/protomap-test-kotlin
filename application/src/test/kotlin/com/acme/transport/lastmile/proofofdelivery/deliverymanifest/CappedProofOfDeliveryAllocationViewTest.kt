package com.acme.transport.lastmile.proofofdelivery.deliverymanifest

import kotlin.test.Test
import kotlin.test.assertEquals

class CappedProofOfDeliveryAllocationViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newCappedProofOfDeliveryAllocation("code-465", "title-850")
        val view = toCappedProofOfDeliveryAllocationView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toCappedProofOfDeliveryAllocationViews(mutableListOf<CappedProofOfDeliveryAllocation>(item, item)).size, "views")
    }
}
