package com.acme.transport.customs.broker.tier

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class PrimaryBrokerTierTest {
    @Test
    fun `starts in requested`() {
        val item = newPrimaryBrokerTier("code-558", "label-474")
        assertEquals(PrimaryBrokerTierStatus.REQUESTED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to granted`() {
        val item = newPrimaryBrokerTier("code-558", "label-474")
        try {
            item.moveTo(PrimaryBrokerTierStatus.GRANTED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(PrimaryBrokerTierStatus.GRANTED, item.status, "status")
    }

    @Test
    fun `rejects moves from expired`() {
        val item = newPrimaryBrokerTier("code-558", "label-474")
        item.status = PrimaryBrokerTierStatus.EXPIRED
        assertFails("move") { item.moveTo(PrimaryBrokerTierStatus.REQUESTED) }
    }
}
