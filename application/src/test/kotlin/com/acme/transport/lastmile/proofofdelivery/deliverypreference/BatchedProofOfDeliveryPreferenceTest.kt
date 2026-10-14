package com.acme.transport.lastmile.proofofdelivery.deliverypreference

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class BatchedProofOfDeliveryPreferenceTest {
    @Test
    fun `starts in proposed`() {
        val item = newBatchedProofOfDeliveryPreference("code-326", "label-964")
        assertEquals(BatchedProofOfDeliveryPreferenceStatus.PROPOSED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to approved`() {
        val item = newBatchedProofOfDeliveryPreference("code-326", "label-964")
        try {
            item.moveTo(BatchedProofOfDeliveryPreferenceStatus.APPROVED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(BatchedProofOfDeliveryPreferenceStatus.APPROVED, item.status, "status")
    }

    @Test
    fun `rejects moves from withdrawn`() {
        val item = newBatchedProofOfDeliveryPreference("code-326", "label-964")
        item.status = BatchedProofOfDeliveryPreferenceStatus.WITHDRAWN
        assertFails("move") { item.moveTo(BatchedProofOfDeliveryPreferenceStatus.PROPOSED) }
    }
}
