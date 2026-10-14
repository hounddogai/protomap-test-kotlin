package com.acme.transport.lastmile.proofofdelivery.mapping

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class ProofOfDeliveryDigestTest {
    @Test
    fun `starts in idle`() {
        val item = newProofOfDeliveryDigest("code-957", "label-82")
        assertEquals(ProofOfDeliveryDigestStatus.IDLE, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to draining`() {
        val item = newProofOfDeliveryDigest("code-957", "label-82")
        try {
            item.moveTo(ProofOfDeliveryDigestStatus.DRAINING)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(ProofOfDeliveryDigestStatus.DRAINING, item.status, "status")
    }

    @Test
    fun `rejects moves from stopped`() {
        val item = newProofOfDeliveryDigest("code-957", "label-82")
        item.status = ProofOfDeliveryDigestStatus.STOPPED
        assertFails("move") { item.moveTo(ProofOfDeliveryDigestStatus.IDLE) }
    }
}
