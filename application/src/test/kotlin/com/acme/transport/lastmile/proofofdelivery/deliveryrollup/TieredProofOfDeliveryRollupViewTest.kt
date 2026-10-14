package com.acme.transport.lastmile.proofofdelivery.deliveryrollup

import kotlin.test.Test
import kotlin.test.assertEquals

class TieredProofOfDeliveryRollupViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newTieredProofOfDeliveryRollup("code-907", "name-56")
        val view = toTieredProofOfDeliveryRollupView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toTieredProofOfDeliveryRollupViews(mutableListOf<TieredProofOfDeliveryRollup>(item, item)).size, "views")
    }
}
