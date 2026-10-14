package com.acme.transport.lastmile.proofofdelivery.estimate

import kotlin.test.Test
import kotlin.test.assertEquals

class AdaptiveProofOfDeliveryThresholdViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newAdaptiveProofOfDeliveryThreshold("code-710", "name-582")
        val view = toAdaptiveProofOfDeliveryThresholdView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toAdaptiveProofOfDeliveryThresholdViews(mutableListOf<AdaptiveProofOfDeliveryThreshold>(item, item)).size, "views")
    }
}
