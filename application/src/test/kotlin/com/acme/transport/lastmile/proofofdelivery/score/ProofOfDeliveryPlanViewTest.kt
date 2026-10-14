package com.acme.transport.lastmile.proofofdelivery.score

import kotlin.test.Test
import kotlin.test.assertEquals

class ProofOfDeliveryPlanViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newProofOfDeliveryPlan("code-956", "title-337")
        val view = toProofOfDeliveryPlanView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toProofOfDeliveryPlanViews(mutableListOf<ProofOfDeliveryPlan>(item, item)).size, "views")
    }
}
