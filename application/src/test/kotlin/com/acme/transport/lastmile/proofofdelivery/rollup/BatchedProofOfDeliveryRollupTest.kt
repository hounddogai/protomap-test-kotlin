package com.acme.transport.lastmile.proofofdelivery.rollup

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class BatchedProofOfDeliveryRollupTest {
    @Test
    fun `starts in draft`() {
        val item = newBatchedProofOfDeliveryRollup("code-936", "title-615")
        assertEquals(BatchedProofOfDeliveryRollupStatus.DRAFT, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to active`() {
        val item = newBatchedProofOfDeliveryRollup("code-936", "title-615")
        try {
            item.moveTo(BatchedProofOfDeliveryRollupStatus.ACTIVE)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(BatchedProofOfDeliveryRollupStatus.ACTIVE, item.status, "status")
    }

    @Test
    fun `rejects moves from closed`() {
        val item = newBatchedProofOfDeliveryRollup("code-936", "title-615")
        item.status = BatchedProofOfDeliveryRollupStatus.CLOSED
        assertFails("move") { item.moveTo(BatchedProofOfDeliveryRollupStatus.DRAFT) }
    }
}
