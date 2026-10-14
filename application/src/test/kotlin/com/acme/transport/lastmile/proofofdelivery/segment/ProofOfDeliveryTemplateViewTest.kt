package com.acme.transport.lastmile.proofofdelivery.segment

import kotlin.test.Test
import kotlin.test.assertEquals

class ProofOfDeliveryTemplateViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newProofOfDeliveryTemplate("code-431", "label-257")
        val view = toProofOfDeliveryTemplateView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toProofOfDeliveryTemplateViews(mutableListOf<ProofOfDeliveryTemplate>(item, item)).size, "views")
    }
}
