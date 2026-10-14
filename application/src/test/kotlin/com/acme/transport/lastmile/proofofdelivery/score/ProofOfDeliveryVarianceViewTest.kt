package com.acme.transport.lastmile.proofofdelivery.score

import kotlin.test.Test
import kotlin.test.assertEquals

class ProofOfDeliveryVarianceViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newProofOfDeliveryVariance("code-44", "label-551")
        val view = toProofOfDeliveryVarianceView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toProofOfDeliveryVarianceViews(mutableListOf<ProofOfDeliveryVariance>(item, item)).size, "views")
    }
}
