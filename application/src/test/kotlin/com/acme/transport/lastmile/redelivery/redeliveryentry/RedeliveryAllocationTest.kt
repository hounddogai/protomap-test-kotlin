package com.acme.transport.lastmile.redelivery.redeliveryentry

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class RedeliveryAllocationTest {
    @Test
    fun `starts in new`() {
        val item = newRedeliveryAllocation("code-650", "name-466")
        assertEquals(RedeliveryAllocationStatus.NEW, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to running`() {
        val item = newRedeliveryAllocation("code-650", "name-466")
        try {
            item.moveTo(RedeliveryAllocationStatus.RUNNING)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(RedeliveryAllocationStatus.RUNNING, item.status, "status")
    }

    @Test
    fun `rejects moves from failed`() {
        val item = newRedeliveryAllocation("code-650", "name-466")
        item.status = RedeliveryAllocationStatus.FAILED
        assertFails("move") { item.moveTo(RedeliveryAllocationStatus.NEW) }
    }
}
