package com.acme.transport.lastmile.proofofdelivery.mapping

import kotlin.test.Test
import kotlin.test.assertEquals

class ProofOfDeliverySlotViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newProofOfDeliverySlot("code-227", "label-34")
        val view = toProofOfDeliverySlotView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toProofOfDeliverySlotViews(mutableListOf<ProofOfDeliverySlot>(item, item)).size, "views")
    }
}
