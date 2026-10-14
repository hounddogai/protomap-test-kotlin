package com.acme.transport.lastmile.proofofdelivery.weighteddeliverymanifest

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class BatchedProofOfDeliveryManifestTest {
    @Test
    fun `starts in open`() {
        val item = newBatchedProofOfDeliveryManifest("code-895", "title-873")
        assertEquals(BatchedProofOfDeliveryManifestStatus.OPEN, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to resolved`() {
        val item = newBatchedProofOfDeliveryManifest("code-895", "title-873")
        try {
            item.moveTo(BatchedProofOfDeliveryManifestStatus.RESOLVED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(BatchedProofOfDeliveryManifestStatus.RESOLVED, item.status, "status")
    }

    @Test
    fun `rejects moves from archived`() {
        val item = newBatchedProofOfDeliveryManifest("code-895", "title-873")
        item.status = BatchedProofOfDeliveryManifestStatus.ARCHIVED
        assertFails("move") { item.moveTo(BatchedProofOfDeliveryManifestStatus.OPEN) }
    }
}
