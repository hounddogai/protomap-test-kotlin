package com.acme.transport.lastmile.proofofdelivery.estimate

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class AdaptiveProofOfDeliveryThresholdTest {
    @Test
    fun `starts in open`() {
        val item = newAdaptiveProofOfDeliveryThreshold("code-710", "name-582")
        assertEquals(AdaptiveProofOfDeliveryThresholdStatus.OPEN, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to reopened`() {
        val item = newAdaptiveProofOfDeliveryThreshold("code-710", "name-582")
        try {
            item.moveTo(AdaptiveProofOfDeliveryThresholdStatus.REOPENED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(AdaptiveProofOfDeliveryThresholdStatus.REOPENED, item.status, "status")
    }

    @Test
    fun `rejects moves from archived`() {
        val item = newAdaptiveProofOfDeliveryThreshold("code-710", "name-582")
        item.status = AdaptiveProofOfDeliveryThresholdStatus.ARCHIVED
        assertFails("move") { item.moveTo(AdaptiveProofOfDeliveryThresholdStatus.OPEN) }
    }
}
