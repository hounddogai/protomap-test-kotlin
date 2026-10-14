package com.acme.transport.lastmile.proofofdelivery.deliverymanifest

import kotlin.test.Test
import kotlin.test.assertEquals

class CappedProofOfDeliveryNoticeViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newCappedProofOfDeliveryNotice("code-946", "name-788")
        val view = toCappedProofOfDeliveryNoticeView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toCappedProofOfDeliveryNoticeViews(mutableListOf<CappedProofOfDeliveryNotice>(item, item)).size, "views")
    }
}
