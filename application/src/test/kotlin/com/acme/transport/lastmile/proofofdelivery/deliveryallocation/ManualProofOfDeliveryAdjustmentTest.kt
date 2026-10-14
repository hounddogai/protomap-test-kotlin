package com.acme.transport.lastmile.proofofdelivery.deliveryallocation

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class ManualProofOfDeliveryAdjustmentTest {
    @Test
    fun `starts in planned`() {
        val item = newManualProofOfDeliveryAdjustment("code-255", "title-998")
        assertEquals(ManualProofOfDeliveryAdjustmentStatus.PLANNED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to completed`() {
        val item = newManualProofOfDeliveryAdjustment("code-255", "title-998")
        try {
            item.moveTo(ManualProofOfDeliveryAdjustmentStatus.COMPLETED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(ManualProofOfDeliveryAdjustmentStatus.COMPLETED, item.status, "status")
    }

    @Test
    fun `rejects moves from cancelled`() {
        val item = newManualProofOfDeliveryAdjustment("code-255", "title-998")
        item.status = ManualProofOfDeliveryAdjustmentStatus.CANCELLED
        assertFails("move") { item.moveTo(ManualProofOfDeliveryAdjustmentStatus.PLANNED) }
    }
}
