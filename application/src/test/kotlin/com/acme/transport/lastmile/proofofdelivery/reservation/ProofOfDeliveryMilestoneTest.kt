package com.acme.transport.lastmile.proofofdelivery.reservation

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class ProofOfDeliveryMilestoneTest {
    @Test
    fun `starts in draft`() {
        val item = newProofOfDeliveryMilestone("code-885", "label-70")
        assertEquals(ProofOfDeliveryMilestoneStatus.DRAFT, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to suspended`() {
        val item = newProofOfDeliveryMilestone("code-885", "label-70")
        try {
            item.moveTo(ProofOfDeliveryMilestoneStatus.SUSPENDED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(ProofOfDeliveryMilestoneStatus.SUSPENDED, item.status, "status")
    }

    @Test
    fun `rejects moves from closed`() {
        val item = newProofOfDeliveryMilestone("code-885", "label-70")
        item.status = ProofOfDeliveryMilestoneStatus.CLOSED
        assertFails("move") { item.moveTo(ProofOfDeliveryMilestoneStatus.DRAFT) }
    }
}
