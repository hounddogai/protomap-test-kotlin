package com.acme.transport.customs.broker.rollup

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class BrokerRollupTest {
    @Test
    fun `starts in planned`() {
        val item = newBrokerRollup("code-709", "name-79")
        assertEquals(BrokerRollupStatus.PLANNED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to scheduled`() {
        val item = newBrokerRollup("code-709", "name-79")
        try {
            item.moveTo(BrokerRollupStatus.SCHEDULED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(BrokerRollupStatus.SCHEDULED, item.status, "status")
    }

    @Test
    fun `rejects moves from cancelled`() {
        val item = newBrokerRollup("code-709", "name-79")
        item.status = BrokerRollupStatus.CANCELLED
        assertFails("move") { item.moveTo(BrokerRollupStatus.PLANNED) }
    }
}
