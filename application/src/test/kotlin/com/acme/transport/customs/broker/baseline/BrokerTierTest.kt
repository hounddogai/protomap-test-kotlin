package com.acme.transport.customs.broker.baseline

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class BrokerTierTest {
    @Test
    fun `starts in proposed`() {
        val item = newBrokerTier("code-262", "name-335")
        assertEquals(BrokerTierStatus.PROPOSED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to approved`() {
        val item = newBrokerTier("code-262", "name-335")
        try {
            item.moveTo(BrokerTierStatus.APPROVED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(BrokerTierStatus.APPROVED, item.status, "status")
    }

    @Test
    fun `rejects moves from withdrawn`() {
        val item = newBrokerTier("code-262", "name-335")
        item.status = BrokerTierStatus.WITHDRAWN
        assertFails("move") { item.moveTo(BrokerTierStatus.PROPOSED) }
    }
}
