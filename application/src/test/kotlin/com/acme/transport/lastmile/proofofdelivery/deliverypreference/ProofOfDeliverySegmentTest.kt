package com.acme.transport.lastmile.proofofdelivery.deliverypreference

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class ProofOfDeliverySegmentTest {
    @Test
    fun `starts in proposed`() {
        val item = newProofOfDeliverySegment("code-105", "name-150")
        assertEquals(ProofOfDeliverySegmentStatus.PROPOSED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to withdrawn`() {
        val item = newProofOfDeliverySegment("code-105", "name-150")
        try {
            item.moveTo(ProofOfDeliverySegmentStatus.WITHDRAWN)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(ProofOfDeliverySegmentStatus.WITHDRAWN, item.status, "status")
    }

    @Test
    fun `rejects moves from withdrawn`() {
        val item = newProofOfDeliverySegment("code-105", "name-150")
        item.status = ProofOfDeliverySegmentStatus.WITHDRAWN
        assertFails("move") { item.moveTo(ProofOfDeliverySegmentStatus.PROPOSED) }
    }
}
