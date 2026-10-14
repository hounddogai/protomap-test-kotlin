package com.acme.transport.lastmile.proofofdelivery.deliveryreview

import kotlin.test.Test
import kotlin.test.assertEquals

class RollingProofOfDeliveryMappingViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newRollingProofOfDeliveryMapping("code-131", "title-935")
        val view = toRollingProofOfDeliveryMappingView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toRollingProofOfDeliveryMappingViews(mutableListOf<RollingProofOfDeliveryMapping>(item, item)).size, "views")
    }
}
