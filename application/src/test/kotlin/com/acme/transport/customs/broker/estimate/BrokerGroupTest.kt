package com.acme.transport.customs.broker.estimate

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class BrokerGroupTest {
    @Test
    fun `starts in planned`() {
        val item = newBrokerGroup("code-377", "name-93")
        assertEquals(BrokerGroupStatus.PLANNED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to completed`() {
        val item = newBrokerGroup("code-377", "name-93")
        try {
            item.moveTo(BrokerGroupStatus.COMPLETED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(BrokerGroupStatus.COMPLETED, item.status, "status")
    }

    @Test
    fun `rejects moves from cancelled`() {
        val item = newBrokerGroup("code-377", "name-93")
        item.status = BrokerGroupStatus.CANCELLED
        assertFails("move") { item.moveTo(BrokerGroupStatus.PLANNED) }
    }
}
