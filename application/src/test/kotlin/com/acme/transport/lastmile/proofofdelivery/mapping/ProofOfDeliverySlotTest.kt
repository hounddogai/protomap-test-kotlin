package com.acme.transport.lastmile.proofofdelivery.mapping

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class ProofOfDeliverySlotTest {
    @Test
    fun `starts in idle`() {
        val item = newProofOfDeliverySlot("code-227", "label-34")
        assertEquals(ProofOfDeliverySlotStatus.IDLE, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to draining`() {
        val item = newProofOfDeliverySlot("code-227", "label-34")
        try {
            item.moveTo(ProofOfDeliverySlotStatus.DRAINING)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(ProofOfDeliverySlotStatus.DRAINING, item.status, "status")
    }

    @Test
    fun `rejects moves from stopped`() {
        val item = newProofOfDeliverySlot("code-227", "label-34")
        item.status = ProofOfDeliverySlotStatus.STOPPED
        assertFails("move") { item.moveTo(ProofOfDeliverySlotStatus.IDLE) }
    }
}
