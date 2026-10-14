package com.acme.transport.lastmile.proofofdelivery.estimate

import kotlin.test.Test
import kotlin.test.assertEquals

class DefaultProofOfDeliveryBundleViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newDefaultProofOfDeliveryBundle("code-86", "name-595")
        val view = toDefaultProofOfDeliveryBundleView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toDefaultProofOfDeliveryBundleViews(mutableListOf<DefaultProofOfDeliveryBundle>(item, item)).size, "views")
    }
}
