package com.acme.transport.customs.broker.slot

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class CappedBrokerSlotTest {
    @Test
    fun `starts in draft`() {
        val item = newCappedBrokerSlot("code-207", "name-151")
        assertEquals(CappedBrokerSlotStatus.DRAFT, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to pending`() {
        val item = newCappedBrokerSlot("code-207", "name-151")
        try {
            item.moveTo(CappedBrokerSlotStatus.PENDING)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(CappedBrokerSlotStatus.PENDING, item.status, "status")
    }

    @Test
    fun `rejects moves from closed`() {
        val item = newCappedBrokerSlot("code-207", "name-151")
        item.status = CappedBrokerSlotStatus.CLOSED
        assertFails("move") { item.moveTo(CappedBrokerSlotStatus.DRAFT) }
    }
}
