package com.acme.transport.lastmile.proofofdelivery.deliveryrollup

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class BatchedProofOfDeliveryPolicyTest {
    @Test
    fun `starts in open`() {
        val item = newBatchedProofOfDeliveryPolicy("code-25", "title-376")
        assertEquals(BatchedProofOfDeliveryPolicyStatus.OPEN, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to archived`() {
        val item = newBatchedProofOfDeliveryPolicy("code-25", "title-376")
        try {
            item.moveTo(BatchedProofOfDeliveryPolicyStatus.ARCHIVED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(BatchedProofOfDeliveryPolicyStatus.ARCHIVED, item.status, "status")
    }

    @Test
    fun `rejects moves from archived`() {
        val item = newBatchedProofOfDeliveryPolicy("code-25", "title-376")
        item.status = BatchedProofOfDeliveryPolicyStatus.ARCHIVED
        assertFails("move") { item.moveTo(BatchedProofOfDeliveryPolicyStatus.OPEN) }
    }
}
