package com.acme.transport.customs.broker.review

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class BrokerCycleTest {
    @Test
    fun `starts in idle`() {
        val item = newBrokerCycle("code-688", "name-450")
        assertEquals(BrokerCycleStatus.IDLE, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to draining`() {
        val item = newBrokerCycle("code-688", "name-450")
        try {
            item.moveTo(BrokerCycleStatus.DRAINING)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(BrokerCycleStatus.DRAINING, item.status, "status")
    }

    @Test
    fun `rejects moves from stopped`() {
        val item = newBrokerCycle("code-688", "name-450")
        item.status = BrokerCycleStatus.STOPPED
        assertFails("move") { item.moveTo(BrokerCycleStatus.IDLE) }
    }
}
