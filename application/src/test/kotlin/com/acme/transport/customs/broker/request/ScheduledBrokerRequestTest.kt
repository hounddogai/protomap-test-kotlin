package com.acme.transport.customs.broker.request

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class ScheduledBrokerRequestTest {
    @Test
    fun `starts in open`() {
        val item = newScheduledBrokerRequest("code-925", "name-958")
        assertEquals(ScheduledBrokerRequestStatus.OPEN, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to resolved`() {
        val item = newScheduledBrokerRequest("code-925", "name-958")
        try {
            item.moveTo(ScheduledBrokerRequestStatus.RESOLVED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(ScheduledBrokerRequestStatus.RESOLVED, item.status, "status")
    }

    @Test
    fun `rejects moves from archived`() {
        val item = newScheduledBrokerRequest("code-925", "name-958")
        item.status = ScheduledBrokerRequestStatus.ARCHIVED
        assertFails("move") { item.moveTo(ScheduledBrokerRequestStatus.OPEN) }
    }
}
