package com.acme.transport.customs.broker.lenientbrokerestimate

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class BrokerClaimTest {
    @Test
    fun `starts in requested`() {
        val item = newBrokerClaim("code-758", "name-655")
        assertEquals(BrokerClaimStatus.REQUESTED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to revoked`() {
        val item = newBrokerClaim("code-758", "name-655")
        try {
            item.moveTo(BrokerClaimStatus.REVOKED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(BrokerClaimStatus.REVOKED, item.status, "status")
    }

    @Test
    fun `rejects moves from expired`() {
        val item = newBrokerClaim("code-758", "name-655")
        item.status = BrokerClaimStatus.EXPIRED
        assertFails("move") { item.moveTo(BrokerClaimStatus.REQUESTED) }
    }
}
