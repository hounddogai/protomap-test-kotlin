package com.acme.transport.lastmile.proofofdelivery.budget

import kotlin.test.Test
import kotlin.test.assertEquals

class ProofOfDeliveryBaselineViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newProofOfDeliveryBaseline("code-926", "label-78")
        val view = toProofOfDeliveryBaselineView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toProofOfDeliveryBaselineViews(mutableListOf<ProofOfDeliveryBaseline>(item, item)).size, "views")
    }
}
