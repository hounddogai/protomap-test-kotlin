package com.acme.transport.lastmile.proofofdelivery.entry

import kotlin.test.Test
import kotlin.test.assertEquals

class AdaptiveProofOfDeliveryEntryViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newAdaptiveProofOfDeliveryEntry("code-877", "title-679")
        val view = toAdaptiveProofOfDeliveryEntryView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toAdaptiveProofOfDeliveryEntryViews(mutableListOf<AdaptiveProofOfDeliveryEntry>(item, item)).size, "views")
    }
}
