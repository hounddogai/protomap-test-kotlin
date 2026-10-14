package com.acme.transport.lastmile.proofofdelivery.baseline

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class WeightedProofOfDeliveryThresholdTest {
    @Test
    fun `starts in proposed`() {
        val item = newWeightedProofOfDeliveryThreshold("code-674", "title-853")
        assertEquals(WeightedProofOfDeliveryThresholdStatus.PROPOSED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to withdrawn`() {
        val item = newWeightedProofOfDeliveryThreshold("code-674", "title-853")
        try {
            item.moveTo(WeightedProofOfDeliveryThresholdStatus.WITHDRAWN)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(WeightedProofOfDeliveryThresholdStatus.WITHDRAWN, item.status, "status")
    }

    @Test
    fun `rejects moves from withdrawn`() {
        val item = newWeightedProofOfDeliveryThreshold("code-674", "title-853")
        item.status = WeightedProofOfDeliveryThresholdStatus.WITHDRAWN
        assertFails("move") { item.moveTo(WeightedProofOfDeliveryThresholdStatus.PROPOSED) }
    }
}
