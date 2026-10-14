package com.acme.transport.lastmile.proofofdelivery.segment

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class WeightedProofOfDeliverySegmentTest {
    @Test
    fun `starts in planned`() {
        val item = newWeightedProofOfDeliverySegment("code-498", "name-95")
        assertEquals(WeightedProofOfDeliverySegmentStatus.PLANNED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to cancelled`() {
        val item = newWeightedProofOfDeliverySegment("code-498", "name-95")
        try {
            item.moveTo(WeightedProofOfDeliverySegmentStatus.CANCELLED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(WeightedProofOfDeliverySegmentStatus.CANCELLED, item.status, "status")
    }

    @Test
    fun `rejects moves from cancelled`() {
        val item = newWeightedProofOfDeliverySegment("code-498", "name-95")
        item.status = WeightedProofOfDeliverySegmentStatus.CANCELLED
        assertFails("move") { item.moveTo(WeightedProofOfDeliverySegmentStatus.PLANNED) }
    }
}
