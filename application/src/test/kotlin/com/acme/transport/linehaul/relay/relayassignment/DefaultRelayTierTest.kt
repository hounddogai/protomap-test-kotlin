package com.acme.transport.linehaul.relay.relayassignment

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class DefaultRelayTierTest {
    @Test
    fun `starts in draft`() {
        val item = newDefaultRelayTier("code-52", "name-455")
        assertEquals(DefaultRelayTierStatus.DRAFT, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to pending`() {
        val item = newDefaultRelayTier("code-52", "name-455")
        try {
            item.moveTo(DefaultRelayTierStatus.PENDING)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(DefaultRelayTierStatus.PENDING, item.status, "status")
    }

    @Test
    fun `rejects moves from closed`() {
        val item = newDefaultRelayTier("code-52", "name-455")
        item.status = DefaultRelayTierStatus.CLOSED
        assertFails("move") { item.moveTo(DefaultRelayTierStatus.DRAFT) }
    }
}
