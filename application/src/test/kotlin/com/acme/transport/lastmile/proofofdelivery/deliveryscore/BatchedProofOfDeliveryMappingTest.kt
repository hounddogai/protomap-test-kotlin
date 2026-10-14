package com.acme.transport.lastmile.proofofdelivery.deliveryscore

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class BatchedProofOfDeliveryMappingTest {
    @Test
    fun `starts in open`() {
        val item = newBatchedProofOfDeliveryMapping("code-697", "name-231")
        assertEquals(BatchedProofOfDeliveryMappingStatus.OPEN, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to in review`() {
        val item = newBatchedProofOfDeliveryMapping("code-697", "name-231")
        try {
            item.moveTo(BatchedProofOfDeliveryMappingStatus.IN_REVIEW)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(BatchedProofOfDeliveryMappingStatus.IN_REVIEW, item.status, "status")
    }

    @Test
    fun `rejects moves from archived`() {
        val item = newBatchedProofOfDeliveryMapping("code-697", "name-231")
        item.status = BatchedProofOfDeliveryMappingStatus.ARCHIVED
        assertFails("move") { item.moveTo(BatchedProofOfDeliveryMappingStatus.OPEN) }
    }
}
