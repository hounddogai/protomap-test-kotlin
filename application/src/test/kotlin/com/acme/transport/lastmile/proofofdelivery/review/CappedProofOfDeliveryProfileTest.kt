package com.acme.transport.lastmile.proofofdelivery.review

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class CappedProofOfDeliveryProfileTest {
    @Test
    fun `starts in idle`() {
        val item = newCappedProofOfDeliveryProfile("code-762", "name-129")
        assertEquals(CappedProofOfDeliveryProfileStatus.IDLE, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to ready`() {
        val item = newCappedProofOfDeliveryProfile("code-762", "name-129")
        try {
            item.moveTo(CappedProofOfDeliveryProfileStatus.READY)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(CappedProofOfDeliveryProfileStatus.READY, item.status, "status")
    }

    @Test
    fun `rejects moves from stopped`() {
        val item = newCappedProofOfDeliveryProfile("code-762", "name-129")
        item.status = CappedProofOfDeliveryProfileStatus.STOPPED
        assertFails("move") { item.moveTo(CappedProofOfDeliveryProfileStatus.IDLE) }
    }
}
