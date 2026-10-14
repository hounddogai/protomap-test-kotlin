package com.acme.transport.lastmile.proofofdelivery.reservation

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class PendingProofOfDeliveryMilestoneTest {
    @Test
    fun `starts in planned`() {
        val item = newPendingProofOfDeliveryMilestone("code-444", "label-145")
        assertEquals(PendingProofOfDeliveryMilestoneStatus.PLANNED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to in progress`() {
        val item = newPendingProofOfDeliveryMilestone("code-444", "label-145")
        try {
            item.moveTo(PendingProofOfDeliveryMilestoneStatus.IN_PROGRESS)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(PendingProofOfDeliveryMilestoneStatus.IN_PROGRESS, item.status, "status")
    }

    @Test
    fun `rejects moves from cancelled`() {
        val item = newPendingProofOfDeliveryMilestone("code-444", "label-145")
        item.status = PendingProofOfDeliveryMilestoneStatus.CANCELLED
        assertFails("move") { item.moveTo(PendingProofOfDeliveryMilestoneStatus.PLANNED) }
    }
}
