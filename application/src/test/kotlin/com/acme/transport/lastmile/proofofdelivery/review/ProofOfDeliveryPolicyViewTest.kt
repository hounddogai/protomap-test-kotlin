package com.acme.transport.lastmile.proofofdelivery.review

import kotlin.test.Test
import kotlin.test.assertEquals

class ProofOfDeliveryPolicyViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newProofOfDeliveryPolicy("code-142", "name-173")
        val view = toProofOfDeliveryPolicyView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toProofOfDeliveryPolicyViews(mutableListOf<ProofOfDeliveryPolicy>(item, item)).size, "views")
    }
}
