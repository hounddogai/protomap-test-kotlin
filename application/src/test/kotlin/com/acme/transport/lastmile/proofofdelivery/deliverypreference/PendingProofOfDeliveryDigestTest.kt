package com.acme.transport.lastmile.proofofdelivery.deliverypreference

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class PendingProofOfDeliveryDigestTest {
    @Test
    fun `starts in idle`() {
        val item = newPendingProofOfDeliveryDigest("code-48", "label-212")
        assertEquals(PendingProofOfDeliveryDigestStatus.IDLE, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to draining`() {
        val item = newPendingProofOfDeliveryDigest("code-48", "label-212")
        try {
            item.moveTo(PendingProofOfDeliveryDigestStatus.DRAINING)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(PendingProofOfDeliveryDigestStatus.DRAINING, item.status, "status")
    }

    @Test
    fun `rejects moves from stopped`() {
        val item = newPendingProofOfDeliveryDigest("code-48", "label-212")
        item.status = PendingProofOfDeliveryDigestStatus.STOPPED
        assertFails("move") { item.moveTo(PendingProofOfDeliveryDigestStatus.IDLE) }
    }
}
