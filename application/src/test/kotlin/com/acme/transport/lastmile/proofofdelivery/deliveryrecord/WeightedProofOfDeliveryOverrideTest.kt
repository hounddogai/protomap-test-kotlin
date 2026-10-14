package com.acme.transport.lastmile.proofofdelivery.deliveryrecord

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class WeightedProofOfDeliveryOverrideTest {
    @Test
    fun `starts in open`() {
        val item = newWeightedProofOfDeliveryOverride("code-621", "title-365")
        assertEquals(WeightedProofOfDeliveryOverrideStatus.OPEN, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to in review`() {
        val item = newWeightedProofOfDeliveryOverride("code-621", "title-365")
        try {
            item.moveTo(WeightedProofOfDeliveryOverrideStatus.IN_REVIEW)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(WeightedProofOfDeliveryOverrideStatus.IN_REVIEW, item.status, "status")
    }

    @Test
    fun `rejects moves from archived`() {
        val item = newWeightedProofOfDeliveryOverride("code-621", "title-365")
        item.status = WeightedProofOfDeliveryOverrideStatus.ARCHIVED
        assertFails("move") { item.moveTo(WeightedProofOfDeliveryOverrideStatus.OPEN) }
    }
}
