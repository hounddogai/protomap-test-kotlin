package com.acme.transport.customs.broker.profile

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class BrokerProfileTest {
    @Test
    fun `starts in planned`() {
        val item = newBrokerProfile("code-182", "title-660")
        assertEquals(BrokerProfileStatus.PLANNED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to cancelled`() {
        val item = newBrokerProfile("code-182", "title-660")
        try {
            item.moveTo(BrokerProfileStatus.CANCELLED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(BrokerProfileStatus.CANCELLED, item.status, "status")
    }

    @Test
    fun `rejects moves from cancelled`() {
        val item = newBrokerProfile("code-182", "title-660")
        item.status = BrokerProfileStatus.CANCELLED
        assertFails("move") { item.moveTo(BrokerProfileStatus.PLANNED) }
    }
}
