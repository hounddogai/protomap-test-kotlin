package com.acme.transport.lastmile.proofofdelivery.policy

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class PendingProofOfDeliveryBaselineTest {
    @Test
    fun `starts in idle`() {
        val item = newPendingProofOfDeliveryBaseline("code-608", "title-45")
        assertEquals(PendingProofOfDeliveryBaselineStatus.IDLE, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to ready`() {
        val item = newPendingProofOfDeliveryBaseline("code-608", "title-45")
        try {
            item.moveTo(PendingProofOfDeliveryBaselineStatus.READY)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(PendingProofOfDeliveryBaselineStatus.READY, item.status, "status")
    }

    @Test
    fun `rejects moves from stopped`() {
        val item = newPendingProofOfDeliveryBaseline("code-608", "title-45")
        item.status = PendingProofOfDeliveryBaselineStatus.STOPPED
        assertFails("move") { item.moveTo(PendingProofOfDeliveryBaselineStatus.IDLE) }
    }
}
