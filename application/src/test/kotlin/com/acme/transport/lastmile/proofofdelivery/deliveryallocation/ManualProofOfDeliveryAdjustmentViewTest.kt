package com.acme.transport.lastmile.proofofdelivery.deliveryallocation

import kotlin.test.Test
import kotlin.test.assertEquals

class ManualProofOfDeliveryAdjustmentViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newManualProofOfDeliveryAdjustment("code-255", "title-998")
        val view = toManualProofOfDeliveryAdjustmentView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toManualProofOfDeliveryAdjustmentViews(mutableListOf<ManualProofOfDeliveryAdjustment>(item, item)).size, "views")
    }
}
