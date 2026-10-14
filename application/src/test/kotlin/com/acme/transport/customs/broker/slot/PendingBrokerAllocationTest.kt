package com.acme.transport.customs.broker.slot

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class PendingBrokerAllocationTest {
    @Test
    fun `starts in draft`() {
        val item = newPendingBrokerAllocation("code-716", "name-127")
        assertEquals(PendingBrokerAllocationStatus.DRAFT, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to active`() {
        val item = newPendingBrokerAllocation("code-716", "name-127")
        try {
            item.moveTo(PendingBrokerAllocationStatus.ACTIVE)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(PendingBrokerAllocationStatus.ACTIVE, item.status, "status")
    }

    @Test
    fun `rejects moves from closed`() {
        val item = newPendingBrokerAllocation("code-716", "name-127")
        item.status = PendingBrokerAllocationStatus.CLOSED
        assertFails("move") { item.moveTo(PendingBrokerAllocationStatus.DRAFT) }
    }
}
