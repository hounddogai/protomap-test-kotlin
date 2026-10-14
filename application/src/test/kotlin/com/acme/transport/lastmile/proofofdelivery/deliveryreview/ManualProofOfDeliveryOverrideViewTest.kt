package com.acme.transport.lastmile.proofofdelivery.deliveryreview

import kotlin.test.Test
import kotlin.test.assertEquals

class ManualProofOfDeliveryOverrideViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newManualProofOfDeliveryOverride("code-613", "name-343")
        val view = toManualProofOfDeliveryOverrideView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toManualProofOfDeliveryOverrideViews(mutableListOf<ManualProofOfDeliveryOverride>(item, item)).size, "views")
    }
}
