package com.acme.transport.lastmile.proofofdelivery.template

import kotlin.test.Test
import kotlin.test.assertEquals

class LenientProofOfDeliveryProfileViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newLenientProofOfDeliveryProfile("code-790", "title-477")
        val view = toLenientProofOfDeliveryProfileView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toLenientProofOfDeliveryProfileViews(mutableListOf<LenientProofOfDeliveryProfile>(item, item)).size, "views")
    }
}
