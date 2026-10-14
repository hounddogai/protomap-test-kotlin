package com.acme.transport.lastmile.proofofdelivery.window

import kotlin.test.Test
import kotlin.test.assertEquals

class ProofOfDeliveryRollupViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newProofOfDeliveryRollup("code-556", "title-57")
        val view = toProofOfDeliveryRollupView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toProofOfDeliveryRollupViews(mutableListOf<ProofOfDeliveryRollup>(item, item)).size, "views")
    }
}
