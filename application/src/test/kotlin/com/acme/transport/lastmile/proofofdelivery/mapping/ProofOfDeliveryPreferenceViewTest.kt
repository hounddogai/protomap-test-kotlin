package com.acme.transport.lastmile.proofofdelivery.mapping

import kotlin.test.Test
import kotlin.test.assertEquals

class ProofOfDeliveryPreferenceViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newProofOfDeliveryPreference("code-343", "label-145")
        val view = toProofOfDeliveryPreferenceView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toProofOfDeliveryPreferenceViews(mutableListOf<ProofOfDeliveryPreference>(item, item)).size, "views")
    }
}
