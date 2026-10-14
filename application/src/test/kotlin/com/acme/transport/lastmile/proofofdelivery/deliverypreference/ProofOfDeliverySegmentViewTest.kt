package com.acme.transport.lastmile.proofofdelivery.deliverypreference

import kotlin.test.Test
import kotlin.test.assertEquals

class ProofOfDeliverySegmentViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newProofOfDeliverySegment("code-105", "name-150")
        val view = toProofOfDeliverySegmentView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toProofOfDeliverySegmentViews(mutableListOf<ProofOfDeliverySegment>(item, item)).size, "views")
    }
}
