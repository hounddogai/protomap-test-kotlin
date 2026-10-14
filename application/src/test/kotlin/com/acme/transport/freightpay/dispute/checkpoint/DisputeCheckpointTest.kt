package com.acme.transport.freightpay.dispute.checkpoint

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class DisputeCheckpointTest {
    @Test
    fun `starts in draft`() {
        val item = newDisputeCheckpoint("code-935", "label-386")
        assertEquals(DisputeCheckpointStatus.DRAFT, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to active`() {
        val item = newDisputeCheckpoint("code-935", "label-386")
        try {
            item.moveTo(DisputeCheckpointStatus.ACTIVE)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(DisputeCheckpointStatus.ACTIVE, item.status, "status")
    }

    @Test
    fun `rejects moves from closed`() {
        val item = newDisputeCheckpoint("code-935", "label-386")
        item.status = DisputeCheckpointStatus.CLOSED
        assertFails("move") { item.moveTo(DisputeCheckpointStatus.DRAFT) }
    }
}
