package com.acme.transport.customs.broker.allocation

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class BrokerAllocationTest {
    @Test
    fun `starts in new`() {
        val item = newBrokerAllocation("code-978", "title-48")
        assertEquals(BrokerAllocationStatus.NEW, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to queued`() {
        val item = newBrokerAllocation("code-978", "title-48")
        try {
            item.moveTo(BrokerAllocationStatus.QUEUED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(BrokerAllocationStatus.QUEUED, item.status, "status")
    }

    @Test
    fun `rejects moves from failed`() {
        val item = newBrokerAllocation("code-978", "title-48")
        item.status = BrokerAllocationStatus.FAILED
        assertFails("move") { item.moveTo(BrokerAllocationStatus.NEW) }
    }
}
