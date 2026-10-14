package com.acme.transport.lastmile.proofofdelivery.assignment

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class ProofOfDeliveryAssignmentTest {
    @Test
    fun `starts in planned`() {
        val item = newProofOfDeliveryAssignment("code-361", "label-199")
        assertEquals(ProofOfDeliveryAssignmentStatus.PLANNED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to scheduled`() {
        val item = newProofOfDeliveryAssignment("code-361", "label-199")
        try {
            item.moveTo(ProofOfDeliveryAssignmentStatus.SCHEDULED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(ProofOfDeliveryAssignmentStatus.SCHEDULED, item.status, "status")
    }

    @Test
    fun `rejects moves from cancelled`() {
        val item = newProofOfDeliveryAssignment("code-361", "label-199")
        item.status = ProofOfDeliveryAssignmentStatus.CANCELLED
        assertFails("move") { item.moveTo(ProofOfDeliveryAssignmentStatus.PLANNED) }
    }
}
