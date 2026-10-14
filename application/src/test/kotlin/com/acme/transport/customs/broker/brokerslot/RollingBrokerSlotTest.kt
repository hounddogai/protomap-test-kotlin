package com.acme.transport.customs.broker.brokerslot

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class RollingBrokerSlotTest {
    @Test
    fun `starts in open`() {
        val item = newRollingBrokerSlot("code-606", "name-539")
        assertEquals(RollingBrokerSlotStatus.OPEN, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to in review`() {
        val item = newRollingBrokerSlot("code-606", "name-539")
        try {
            item.moveTo(RollingBrokerSlotStatus.IN_REVIEW)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(RollingBrokerSlotStatus.IN_REVIEW, item.status, "status")
    }

    @Test
    fun `rejects moves from archived`() {
        val item = newRollingBrokerSlot("code-606", "name-539")
        item.status = RollingBrokerSlotStatus.ARCHIVED
        assertFails("move") { item.moveTo(RollingBrokerSlotStatus.OPEN) }
    }
}
