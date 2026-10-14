package com.acme.transport.linehaul.relay.relaythreshold

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class BatchedRelayPlanTest {
    @Test
    fun `starts in draft`() {
        val item = newBatchedRelayPlan("code-403", "label-696")
        assertEquals(BatchedRelayPlanStatus.DRAFT, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to active`() {
        val item = newBatchedRelayPlan("code-403", "label-696")
        try {
            item.moveTo(BatchedRelayPlanStatus.ACTIVE)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(BatchedRelayPlanStatus.ACTIVE, item.status, "status")
    }

    @Test
    fun `rejects moves from closed`() {
        val item = newBatchedRelayPlan("code-403", "label-696")
        item.status = BatchedRelayPlanStatus.CLOSED
        assertFails("move") { item.moveTo(BatchedRelayPlanStatus.DRAFT) }
    }
}
