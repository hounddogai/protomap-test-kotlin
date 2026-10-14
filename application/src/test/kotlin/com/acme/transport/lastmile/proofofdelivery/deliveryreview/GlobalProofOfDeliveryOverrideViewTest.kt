package com.acme.transport.lastmile.proofofdelivery.deliveryreview

import kotlin.test.Test
import kotlin.test.assertEquals

class GlobalProofOfDeliveryOverrideViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newGlobalProofOfDeliveryOverride("code-291", "name-427")
        val view = toGlobalProofOfDeliveryOverrideView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toGlobalProofOfDeliveryOverrideViews(mutableListOf<GlobalProofOfDeliveryOverride>(item, item)).size, "views")
    }
}
