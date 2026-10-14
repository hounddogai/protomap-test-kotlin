package com.acme.transport.lastmile.proofofdelivery.recorditem

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class ProofOfDeliveryBatchTest {
    @Test
    fun `starts in proposed`() {
        val item = newProofOfDeliveryBatch("code-741", "label-367")
        assertEquals(ProofOfDeliveryBatchStatus.PROPOSED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to withdrawn`() {
        val item = newProofOfDeliveryBatch("code-741", "label-367")
        try {
            item.moveTo(ProofOfDeliveryBatchStatus.WITHDRAWN)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(ProofOfDeliveryBatchStatus.WITHDRAWN, item.status, "status")
    }

    @Test
    fun `rejects moves from withdrawn`() {
        val item = newProofOfDeliveryBatch("code-741", "label-367")
        item.status = ProofOfDeliveryBatchStatus.WITHDRAWN
        assertFails("move") { item.moveTo(ProofOfDeliveryBatchStatus.PROPOSED) }
    }
}
