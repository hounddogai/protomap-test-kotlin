package com.acme.transport.lastmile.proofofdelivery.recorditem

import kotlin.test.Test
import kotlin.test.assertEquals

class ProofOfDeliveryBundleViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newProofOfDeliveryBundle("code-759", "title-712")
        val view = toProofOfDeliveryBundleView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toProofOfDeliveryBundleViews(mutableListOf<ProofOfDeliveryBundle>(item, item)).size, "views")
    }
}
