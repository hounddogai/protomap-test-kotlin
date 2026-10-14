package com.acme.transport.lastmile.proofofdelivery.target

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class ProofOfDeliveryTargetTest {
    @Test
    fun `starts in new`() {
        val item = newProofOfDeliveryTarget("code-812", "label-421")
        assertEquals(ProofOfDeliveryTargetStatus.NEW, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to queued`() {
        val item = newProofOfDeliveryTarget("code-812", "label-421")
        try {
            item.moveTo(ProofOfDeliveryTargetStatus.QUEUED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(ProofOfDeliveryTargetStatus.QUEUED, item.status, "status")
    }

    @Test
    fun `rejects moves from failed`() {
        val item = newProofOfDeliveryTarget("code-812", "label-421")
        item.status = ProofOfDeliveryTargetStatus.FAILED
        assertFails("move") { item.moveTo(ProofOfDeliveryTargetStatus.NEW) }
    }
}
