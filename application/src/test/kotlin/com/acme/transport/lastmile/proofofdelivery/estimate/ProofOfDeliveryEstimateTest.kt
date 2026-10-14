package com.acme.transport.lastmile.proofofdelivery.estimate

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class ProofOfDeliveryEstimateTest {
    @Test
    fun `starts in idle`() {
        val item = newProofOfDeliveryEstimate("code-428", "label-965")
        assertEquals(ProofOfDeliveryEstimateStatus.IDLE, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to stopped`() {
        val item = newProofOfDeliveryEstimate("code-428", "label-965")
        try {
            item.moveTo(ProofOfDeliveryEstimateStatus.STOPPED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(ProofOfDeliveryEstimateStatus.STOPPED, item.status, "status")
    }

    @Test
    fun `rejects moves from stopped`() {
        val item = newProofOfDeliveryEstimate("code-428", "label-965")
        item.status = ProofOfDeliveryEstimateStatus.STOPPED
        assertFails("move") { item.moveTo(ProofOfDeliveryEstimateStatus.IDLE) }
    }
}
