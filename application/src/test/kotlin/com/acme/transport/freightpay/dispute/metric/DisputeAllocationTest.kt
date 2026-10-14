package com.acme.transport.freightpay.dispute.metric

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class DisputeAllocationTest {
    @Test
    fun `starts in open`() {
        val item = newDisputeAllocation("code-977", "name-794")
        assertEquals(DisputeAllocationStatus.OPEN, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to resolved`() {
        val item = newDisputeAllocation("code-977", "name-794")
        try {
            item.moveTo(DisputeAllocationStatus.RESOLVED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(DisputeAllocationStatus.RESOLVED, item.status, "status")
    }

    @Test
    fun `rejects moves from archived`() {
        val item = newDisputeAllocation("code-977", "name-794")
        item.status = DisputeAllocationStatus.ARCHIVED
        assertFails("move") { item.moveTo(DisputeAllocationStatus.OPEN) }
    }
}
