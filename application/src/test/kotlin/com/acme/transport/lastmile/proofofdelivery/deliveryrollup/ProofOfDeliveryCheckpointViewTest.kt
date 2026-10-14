package com.acme.transport.lastmile.proofofdelivery.deliveryrollup

import kotlin.test.Test
import kotlin.test.assertEquals

class ProofOfDeliveryCheckpointViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newProofOfDeliveryCheckpoint("code-64", "title-34")
        val view = toProofOfDeliveryCheckpointView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toProofOfDeliveryCheckpointViews(mutableListOf<ProofOfDeliveryCheckpoint>(item, item)).size, "views")
    }
}
