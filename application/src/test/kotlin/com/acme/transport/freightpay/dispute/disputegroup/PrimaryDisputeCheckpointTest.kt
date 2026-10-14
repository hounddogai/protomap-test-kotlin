package com.acme.transport.freightpay.dispute.disputegroup

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class PrimaryDisputeCheckpointTest {
    @Test
    fun `starts in proposed`() {
        val item = newPrimaryDisputeCheckpoint("code-279", "label-683")
        assertEquals(PrimaryDisputeCheckpointStatus.PROPOSED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to rejected`() {
        val item = newPrimaryDisputeCheckpoint("code-279", "label-683")
        try {
            item.moveTo(PrimaryDisputeCheckpointStatus.REJECTED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(PrimaryDisputeCheckpointStatus.REJECTED, item.status, "status")
    }

    @Test
    fun `rejects moves from withdrawn`() {
        val item = newPrimaryDisputeCheckpoint("code-279", "label-683")
        item.status = PrimaryDisputeCheckpointStatus.WITHDRAWN
        assertFails("move") { item.moveTo(PrimaryDisputeCheckpointStatus.PROPOSED) }
    }
}
