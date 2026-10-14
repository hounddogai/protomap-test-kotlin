package com.acme.transport.lastmile.proofofdelivery.deliveryrollup

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class ProofOfDeliveryCheckpointTest {
    @Test
    fun `starts in proposed`() {
        val item = newProofOfDeliveryCheckpoint("code-64", "title-34")
        assertEquals(ProofOfDeliveryCheckpointStatus.PROPOSED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to withdrawn`() {
        val item = newProofOfDeliveryCheckpoint("code-64", "title-34")
        try {
            item.moveTo(ProofOfDeliveryCheckpointStatus.WITHDRAWN)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(ProofOfDeliveryCheckpointStatus.WITHDRAWN, item.status, "status")
    }

    @Test
    fun `rejects moves from withdrawn`() {
        val item = newProofOfDeliveryCheckpoint("code-64", "title-34")
        item.status = ProofOfDeliveryCheckpointStatus.WITHDRAWN
        assertFails("move") { item.moveTo(ProofOfDeliveryCheckpointStatus.PROPOSED) }
    }
}
