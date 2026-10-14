package com.acme.transport.customs.broker.budget

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class BrokerRequestTest {
    @Test
    fun `starts in planned`() {
        val item = newBrokerRequest("code-721", "label-71")
        assertEquals(BrokerRequestStatus.PLANNED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to cancelled`() {
        val item = newBrokerRequest("code-721", "label-71")
        try {
            item.moveTo(BrokerRequestStatus.CANCELLED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(BrokerRequestStatus.CANCELLED, item.status, "status")
    }

    @Test
    fun `rejects moves from cancelled`() {
        val item = newBrokerRequest("code-721", "label-71")
        item.status = BrokerRequestStatus.CANCELLED
        assertFails("move") { item.moveTo(BrokerRequestStatus.PLANNED) }
    }
}
