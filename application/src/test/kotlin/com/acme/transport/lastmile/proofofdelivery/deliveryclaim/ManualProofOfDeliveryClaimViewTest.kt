package com.acme.transport.lastmile.proofofdelivery.deliveryclaim

import kotlin.test.Test
import kotlin.test.assertEquals

class ManualProofOfDeliveryClaimViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newManualProofOfDeliveryClaim("code-168", "label-549")
        val view = toManualProofOfDeliveryClaimView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toManualProofOfDeliveryClaimViews(mutableListOf<ManualProofOfDeliveryClaim>(item, item)).size, "views")
    }
}
