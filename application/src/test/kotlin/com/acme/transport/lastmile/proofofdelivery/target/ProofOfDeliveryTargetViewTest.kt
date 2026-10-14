package com.acme.transport.lastmile.proofofdelivery.target

import kotlin.test.Test
import kotlin.test.assertEquals

class ProofOfDeliveryTargetViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newProofOfDeliveryTarget("code-812", "label-421")
        val view = toProofOfDeliveryTargetView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toProofOfDeliveryTargetViews(mutableListOf<ProofOfDeliveryTarget>(item, item)).size, "views")
    }
}
