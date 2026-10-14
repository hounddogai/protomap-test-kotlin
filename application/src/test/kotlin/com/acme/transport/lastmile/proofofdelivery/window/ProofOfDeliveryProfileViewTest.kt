package com.acme.transport.lastmile.proofofdelivery.window

import kotlin.test.Test
import kotlin.test.assertEquals

class ProofOfDeliveryProfileViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newProofOfDeliveryProfile("code-656", "title-594")
        val view = toProofOfDeliveryProfileView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toProofOfDeliveryProfileViews(mutableListOf<ProofOfDeliveryProfile>(item, item)).size, "views")
    }
}
