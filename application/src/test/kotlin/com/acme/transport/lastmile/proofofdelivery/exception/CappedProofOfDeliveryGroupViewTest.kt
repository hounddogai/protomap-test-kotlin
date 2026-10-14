package com.acme.transport.lastmile.proofofdelivery.exception

import kotlin.test.Test
import kotlin.test.assertEquals

class CappedProofOfDeliveryGroupViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newCappedProofOfDeliveryGroup("code-247", "label-933")
        val view = toCappedProofOfDeliveryGroupView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toCappedProofOfDeliveryGroupViews(mutableListOf<CappedProofOfDeliveryGroup>(item, item)).size, "views")
    }
}
