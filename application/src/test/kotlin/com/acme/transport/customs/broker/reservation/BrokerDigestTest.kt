package com.acme.transport.customs.broker.reservation

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class BrokerDigestTest {
    @Test
    fun `starts in requested`() {
        val item = newBrokerDigest("code-307", "label-438")
        assertEquals(BrokerDigestStatus.REQUESTED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to expired`() {
        val item = newBrokerDigest("code-307", "label-438")
        try {
            item.moveTo(BrokerDigestStatus.EXPIRED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(BrokerDigestStatus.EXPIRED, item.status, "status")
    }

    @Test
    fun `rejects moves from expired`() {
        val item = newBrokerDigest("code-307", "label-438")
        item.status = BrokerDigestStatus.EXPIRED
        assertFails("move") { item.moveTo(BrokerDigestStatus.REQUESTED) }
    }
}
