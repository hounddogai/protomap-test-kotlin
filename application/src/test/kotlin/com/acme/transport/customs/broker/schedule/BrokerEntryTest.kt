package com.acme.transport.customs.broker.schedule

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class BrokerEntryTest {
    @Test
    fun `starts in created`() {
        val item = newBrokerEntry("code-35", "name-530")
        assertEquals(BrokerEntryStatus.CREATED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to validated`() {
        val item = newBrokerEntry("code-35", "name-530")
        try {
            item.moveTo(BrokerEntryStatus.VALIDATED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(BrokerEntryStatus.VALIDATED, item.status, "status")
    }

    @Test
    fun `rejects moves from retired`() {
        val item = newBrokerEntry("code-35", "name-530")
        item.status = BrokerEntryStatus.RETIRED
        assertFails("move") { item.moveTo(BrokerEntryStatus.CREATED) }
    }
}
