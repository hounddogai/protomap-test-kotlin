package com.acme.transport.customs.broker.review

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class RegionalBrokerRequestTest {
    @Test
    fun `starts in planned`() {
        val item = newRegionalBrokerRequest("code-291", "title-771")
        assertEquals(RegionalBrokerRequestStatus.PLANNED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to completed`() {
        val item = newRegionalBrokerRequest("code-291", "title-771")
        try {
            item.moveTo(RegionalBrokerRequestStatus.COMPLETED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(RegionalBrokerRequestStatus.COMPLETED, item.status, "status")
    }

    @Test
    fun `rejects moves from cancelled`() {
        val item = newRegionalBrokerRequest("code-291", "title-771")
        item.status = RegionalBrokerRequestStatus.CANCELLED
        assertFails("move") { item.moveTo(RegionalBrokerRequestStatus.PLANNED) }
    }
}
