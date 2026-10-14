package com.acme.transport.lastmile.proofofdelivery.score

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class ProofOfDeliveryVarianceTest {
    @Test
    fun `starts in idle`() {
        val item = newProofOfDeliveryVariance("code-44", "label-551")
        assertEquals(ProofOfDeliveryVarianceStatus.IDLE, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to ready`() {
        val item = newProofOfDeliveryVariance("code-44", "label-551")
        try {
            item.moveTo(ProofOfDeliveryVarianceStatus.READY)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(ProofOfDeliveryVarianceStatus.READY, item.status, "status")
    }

    @Test
    fun `rejects moves from stopped`() {
        val item = newProofOfDeliveryVariance("code-44", "label-551")
        item.status = ProofOfDeliveryVarianceStatus.STOPPED
        assertFails("move") { item.moveTo(ProofOfDeliveryVarianceStatus.IDLE) }
    }
}
